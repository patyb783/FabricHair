package com.fabricahair.controller;

import com.fabricahair.repository.*;
import com.fabricahair.service.ProdutoService;
import com.fabricahair.service.InsumoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private OrdemProducaoRepository opRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private PedidoVendaRepository pedidoRepository;
    @Autowired
    private InsumoRepository insumoRepository;
    @Autowired
    private TituloFinanceiroRepository tituloFinanceiroRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // KPIs Estratégicos C-Level e Operacionais
        model.addAttribute("opAbertas",
                opRepository.findByStatusIn(List.of(
                        com.fabricahair.model.OrdemProducao.StatusOP.PLANEJADA,
                        com.fabricahair.model.OrdemProducao.StatusOP.EM_PRODUCAO)).size());

        model.addAttribute("lotesProntos",
                loteRepository.findByStatus(com.fabricahair.model.Lote.StatusLote.APROVADO).size());

        model.addAttribute("pedidosPendentes",
                pedidoRepository.findByStatus(com.fabricahair.model.PedidoVenda.StatusPedido.CONFIRMADO).size());

        model.addAttribute("insumosAbaixoMinimo",
                insumoRepository.findInsumosAbaixoDoMinimo().size());

        // KPIs Estratégicos Financeiros
        var aReceber = tituloFinanceiroRepository.findByTipoAndStatusOrderByDataVencimentoAsc(
                com.fabricahair.model.TituloFinanceiro.TipoTitulo.RECEBER,
                com.fabricahair.model.TituloFinanceiro.StatusTitulo.PENDENTE);

        var aPagar = tituloFinanceiroRepository.findByTipoAndStatusOrderByDataVencimentoAsc(
                com.fabricahair.model.TituloFinanceiro.TipoTitulo.PAGAR,
                com.fabricahair.model.TituloFinanceiro.StatusTitulo.PENDENTE);

        java.math.BigDecimal totalReceber = aReceber.stream()
                .map(com.fabricahair.model.TituloFinanceiro::getValorOriginal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalPagar = aPagar.stream()
                .map(com.fabricahair.model.TituloFinanceiro::getValorOriginal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        model.addAttribute("totalReceber", totalReceber);
        model.addAttribute("totalPagar", totalPagar);

        // KPI Entrega no Prazo (OTIF)
        var entregues = pedidoRepository.findByStatus(com.fabricahair.model.PedidoVenda.StatusPedido.ENTREGUE);
        long noPrazo = entregues.stream()
                .filter(p -> "NO PRAZO".equalsIgnoreCase(p.getOtifAvaliacao()))
                .count();
        double otifPercentual = entregues.isEmpty() ? 100.0 : (noPrazo * 100.0) / entregues.size();
        model.addAttribute("otifPercentual", Math.round(otifPercentual));

        // Listas para widgets
        model.addAttribute("insumosAlerta", insumoRepository.findInsumosAbaixoDoMinimo());

        var todasOps = opRepository.findAll();
        model.addAttribute("ultimasOPs",
                todasOps.size() > 5 ? todasOps.subList(todasOps.size() - 5, todasOps.size()) : todasOps);

        var todosPedidos = pedidoRepository.findAll();
        model.addAttribute("ultimosPedidos",
                todosPedidos.size() > 5 ? todosPedidos.subList(todosPedidos.size() - 5, todosPedidos.size())
                        : todosPedidos);

        return "dashboard/index";
    }
}
