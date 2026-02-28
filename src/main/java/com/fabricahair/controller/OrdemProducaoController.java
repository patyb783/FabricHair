package com.fabricahair.controller;

import com.fabricahair.model.*;
import com.fabricahair.repository.InsumoRepository;
import com.fabricahair.repository.ProdutoAcabadoRepository;
import com.fabricahair.service.OrdemProducaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/web/producao/ordens")
public class OrdemProducaoController {

    @Autowired
    private OrdemProducaoService opService;
    @Autowired
    private ProdutoAcabadoRepository produtoRepository;
    @Autowired
    private InsumoRepository insumoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ordens", opService.listarTodas());
        return "producao/listar";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("produtos", produtoRepository.findByAtivoTrue());
        model.addAttribute("insumos", insumoRepository.findByAtivoTrue());
        return "producao/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Long produtoId,
            @RequestParam BigDecimal quantidade,
            @RequestParam(required = false) List<Long> insumoIds,
            @RequestParam(required = false) List<BigDecimal> quantidadesInsumos,
            @RequestParam(required = false) String observacoes,
            RedirectAttributes ra) {
        try {
            OrdemProducao op = opService.criar(produtoId, quantidade, insumoIds, quantidadesInsumos, observacoes);
            ra.addFlashAttribute("sucesso", "Ordem de Produção " + op.getNumeroOp() + " criada com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao criar OP: " + e.getMessage());
        }
        return "redirect:/web/producao/ordens";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        OrdemProducao op = opService.buscarPorId(id);
        model.addAttribute("op", op);
        return "producao/detalhe";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            OrdemProducao op = opService.iniciarProducao(id);
            ra.addFlashAttribute("sucesso",
                    "OP " + op.getNumeroOp() + " iniciada! Insumos baixados do estoque.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/producao/ordens/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id,
            @RequestParam BigDecimal quantidadeProduzida,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validade,
            @RequestParam(required = false) String observacoesLote,
            @RequestParam(required = false) BigDecimal quantidadeRefugo,
            @RequestParam(required = false) String motivoRefugo,
            @RequestParam(required = false) BigDecimal custoMaoDeObra,
            @RequestParam(required = false) BigDecimal custoEnergia,
            RedirectAttributes ra) {
        try {
            OrdemProducao op = opService.finalizarProducao(id, quantidadeProduzida, validade, observacoesLote,
                    quantidadeRefugo, motivoRefugo, custoMaoDeObra, custoEnergia);
            ra.addFlashAttribute("sucesso",
                    "OP " + op.getNumeroOp() + " finalizada! Lote aguardando liberação na Quarentena.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/producao/ordens/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            opService.cancelar(id);
            ra.addFlashAttribute("sucesso", "OP cancelada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/producao/ordens";
    }
}
