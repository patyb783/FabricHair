package com.fabricahair.controller;

import com.fabricahair.model.Lote;
import com.fabricahair.model.PedidoItem;
import com.fabricahair.model.PedidoVenda;
import com.fabricahair.repository.LoteRepository;
import com.fabricahair.service.PedidoVendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/web/logistica")
public class ExpedicaoController {

    @Autowired
    private PedidoVendaService pedidoService;

    @Autowired
    private LoteRepository loteRepository;

    @GetMapping("/picking/{id}")
    public String pickingList(@PathVariable Long id, Model model) {
        PedidoVenda pedido = pedidoService.buscarPorId(id);

        List<PickingItemDto> pickingList = new ArrayList<>();

        for (PedidoItem item : pedido.getItens()) {
            PickingItemDto dto = new PickingItemDto();
            dto.setProdutoNome(item.getProduto().getNome());
            dto.setSku(item.getProduto().getSku());
            dto.setQuantidade(item.getQuantidade());
            dto.setLocalizacaoPadrao(
                    item.getProduto().getLocalizacaoPadrao() != null ? item.getProduto().getLocalizacaoPadrao()
                            : "Sem endereço p/ prod");

            // Sugestão FEFO (First-Expire, First-Out)
            List<Lote> lotesAprovados = loteRepository.findByProdutoIdAndStatus(item.getProduto().getId(),
                    Lote.StatusLote.APROVADO);
            // Ordenar por data de validade (os que vencem primeiro ficam no topo)
            lotesAprovados
                    .sort(Comparator.comparing(l -> l.getDataValidade() != null ? l.getDataValidade() : LocalDate.MAX));

            if (!lotesAprovados.isEmpty()) {
                Lote sugestao = lotesAprovados.get(0);
                dto.setLoteSugerido(sugestao.getNumeroLote());
                dto.setLoteLocalizacao(
                        sugestao.getLocalizacao() != null ? sugestao.getLocalizacao() : dto.getLocalizacaoPadrao());
            } else {
                dto.setLoteSugerido("Sem lote aprovado");
                dto.setLoteLocalizacao("-");
            }
            pickingList.add(dto);
        }

        // Ordena pelo endereço físico para poupar passos do estoquista
        pickingList.sort(Comparator.comparing(PickingItemDto::getLocalizacaoPadrao));

        model.addAttribute("pedido", pedido);
        model.addAttribute("pickingList", pickingList);
        return "logistica/picking";
    }

    public static class PickingItemDto {
        private String produtoNome;
        private String sku;
        private BigDecimal quantidade;
        private String localizacaoPadrao;
        private String loteSugerido;
        private String loteLocalizacao;

        public String getProdutoNome() {
            return produtoNome;
        }

        public void setProdutoNome(String produtoNome) {
            this.produtoNome = produtoNome;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public BigDecimal getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(BigDecimal quantidade) {
            this.quantidade = quantidade;
        }

        public String getLocalizacaoPadrao() {
            return localizacaoPadrao;
        }

        public void setLocalizacaoPadrao(String localizacaoPadrao) {
            this.localizacaoPadrao = localizacaoPadrao;
        }

        public String getLoteSugerido() {
            return loteSugerido;
        }

        public void setLoteSugerido(String loteSugerido) {
            this.loteSugerido = loteSugerido;
        }

        public String getLoteLocalizacao() {
            return loteLocalizacao;
        }

        public void setLoteLocalizacao(String loteLocalizacao) {
            this.loteLocalizacao = loteLocalizacao;
        }
    }
}
