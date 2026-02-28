package com.fabricahair.controller;

import com.fabricahair.model.PedidoVenda;
import com.fabricahair.model.ProdutoAcabado;
import com.fabricahair.model.PedidoItem;
import com.fabricahair.repository.PedidoVendaRepository;
import com.fabricahair.repository.ProdutoAcabadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/logistica/painel")
public class LogisticaController {

    @Autowired
    private PedidoVendaRepository pedidoRepository;
    @Autowired
    private ProdutoAcabadoRepository produtoRepository;

    @GetMapping
    public String painel(Model model) {
        List<PedidoVenda> todos = pedidoRepository.findAll();

        List<PedidoVenda> faturados = todos.stream()
                .filter(p -> p.getStatus() == PedidoVenda.StatusPedido.FATURADO)
                .collect(Collectors.toList());

        List<PedidoVenda> entregues = todos.stream()
                .filter(p -> p.getStatus() == PedidoVenda.StatusPedido.ENTREGUE && p.getOtifAvaliacao() != null)
                .collect(Collectors.toList());

        long totalEntregues = entregues.size();
        long noPrazo = entregues.stream().filter(p -> "NO PRAZO".equals(p.getOtifAvaliacao())).count();
        int otifPerc = totalEntregues > 0 ? (int) ((noPrazo * 100) / totalEntregues) : 0;

        // Curva ABC de Produtos (Últimos 30 dias)
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Map<Long, BigDecimal> volumePorProduto = new HashMap<>();

        List<PedidoVenda> recentes = todos.stream()
                .filter(p -> p.getCriadoEm() != null && p.getCriadoEm().isAfter(limite)
                        && p.getStatus() != PedidoVenda.StatusPedido.CANCELADO)
                .collect(Collectors.toList());

        for (PedidoVenda p : recentes) {
            for (PedidoItem item : p.getItens()) {
                if (item.getProduto() != null) {
                    volumePorProduto.merge(item.getProduto().getId(), item.getQuantidade(), BigDecimal::add);
                }
            }
        }

        List<ProdutoCurvaABC> ranking = new ArrayList<>();
        List<ProdutoAcabado> prods = produtoRepository.findAll();
        BigDecimal volumeTotalGeral = volumePorProduto.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        for (ProdutoAcabado prod : prods) {
            BigDecimal vol = volumePorProduto.getOrDefault(prod.getId(), BigDecimal.ZERO);
            if (vol.compareTo(BigDecimal.ZERO) > 0) {
                ranking.add(new ProdutoCurvaABC(prod, vol, BigDecimal.ZERO, ""));
            }
        }

        // Ordenar descrescente
        ranking.sort((a, b) -> b.getVolume().compareTo(a.getVolume()));

        // Calcular Acumulado e Classificar A (80%), B (15%), C (5%)
        BigDecimal acumulado = BigDecimal.ZERO;
        for (ProdutoCurvaABC item : ranking) {
            acumulado = acumulado.add(item.getVolume());
            item.setPercAcumulado(volumeTotalGeral.compareTo(BigDecimal.ZERO) > 0
                    ? acumulado.divide(volumeTotalGeral, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO);

            if (item.getPercAcumulado().compareTo(new BigDecimal("80")) <= 0) {
                item.setCurva("A");
            } else if (item.getPercAcumulado().compareTo(new BigDecimal("95")) <= 0) {
                item.setCurva("B");
            } else {
                item.setCurva("C");
            }
        }

        model.addAttribute("otifPerc", otifPerc);
        model.addAttribute("faturados", faturados);
        model.addAttribute("curvaAbc", ranking);

        return "logistica/painel";
    }

    // DTO Helper interno
    public static class ProdutoCurvaABC {
        private ProdutoAcabado produto;
        private BigDecimal volume;
        private BigDecimal percAcumulado;
        private String curva;

        public ProdutoCurvaABC(ProdutoAcabado p, BigDecimal v, BigDecimal perc, String c) {
            this.produto = p;
            this.volume = v;
            this.percAcumulado = perc;
            this.curva = c;
        }

        public ProdutoAcabado getProduto() {
            return produto;
        }

        public BigDecimal getVolume() {
            return volume;
        }

        public BigDecimal getPercAcumulado() {
            return percAcumulado;
        }

        public String getCurva() {
            return curva;
        }

        public void setCurva(String c) {
            this.curva = c;
        }

        public void setPercAcumulado(BigDecimal p) {
            this.percAcumulado = p;
        }
    }
}
