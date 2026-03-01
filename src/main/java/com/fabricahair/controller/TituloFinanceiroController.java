package com.fabricahair.controller;

import com.fabricahair.model.TituloFinanceiro;
import com.fabricahair.repository.TituloFinanceiroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/web/financeiro")
public class TituloFinanceiroController {

    @Autowired
    private TituloFinanceiroRepository tituloRepository;

    @GetMapping("/pagar")
    public String listarContasPagar(Model model) {
        model.addAttribute("titulos",
                tituloRepository.findByTipoOrderByDataVencimentoAsc(TituloFinanceiro.TipoTitulo.PAGAR));
        model.addAttribute("tipoPage", "PAGAR");
        model.addAttribute("titlePage", "Contas a Pagar");
        return "financeiro/listar";
    }

    @GetMapping("/receber")
    public String listarContasReceber(Model model) {
        model.addAttribute("titulos",
                tituloRepository.findByTipoOrderByDataVencimentoAsc(TituloFinanceiro.TipoTitulo.RECEBER));
        model.addAttribute("tipoPage", "RECEBER");
        model.addAttribute("titlePage", "Contas a Receber");
        return "financeiro/listar";
    }

    @PostMapping("/nova")
    public String criarAvulso(@RequestParam TituloFinanceiro.TipoTitulo tipo,
            @RequestParam String descricao,
            @RequestParam BigDecimal valorOriginal,
            @RequestParam LocalDate dataVencimento,
            RedirectAttributes ra) {
        TituloFinanceiro titulo = TituloFinanceiro.builder()
                .tipo(tipo)
                .descricao(descricao)
                .valorOriginal(valorOriginal)
                .dataVencimento(dataVencimento)
                .status(TituloFinanceiro.StatusTitulo.PENDENTE)
                .build();
        tituloRepository.save(titulo);
        ra.addFlashAttribute("sucesso", "Título cadastrado com sucesso!");

        return "redirect:/web/financeiro/" + tipo.name().toLowerCase();
    }

    @PostMapping("/{id}/baixar")
    public String darBaixa(@PathVariable Long id,
            @RequestParam BigDecimal valorPago,
            @RequestParam(required = false) String formRedirectTipo,
            RedirectAttributes ra) {
        try {
            TituloFinanceiro titulo = tituloRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Título não encontrado"));
            titulo.setValorPago(valorPago);
            titulo.setDataPagamento(LocalDateTime.now());
            titulo.setStatus(TituloFinanceiro.StatusTitulo.PAGO);
            tituloRepository.save(titulo);

            ra.addFlashAttribute("sucesso", "Título liquidado com sucesso!");
            if (formRedirectTipo != null && !formRedirectTipo.isEmpty()) {
                return "redirect:/web/financeiro/" + formRedirectTipo.toLowerCase();
            }
            return "redirect:/web/financeiro/" + titulo.getTipo().name().toLowerCase();
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/web/financeiro/receber"; // Fallback
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        TituloFinanceiro titulo = tituloRepository.findById(id).orElseThrow();
        titulo.setStatus(TituloFinanceiro.StatusTitulo.CANCELADO);
        tituloRepository.save(titulo);
        ra.addFlashAttribute("sucesso", "Título cancelado.");
        return "redirect:/web/financeiro/" + titulo.getTipo().name().toLowerCase();
    }
}
