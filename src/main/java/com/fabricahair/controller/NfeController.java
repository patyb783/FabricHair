package com.fabricahair.controller;

import com.fabricahair.model.NfeRegistro;
import com.fabricahair.service.NfeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/fiscal")
public class NfeController {

    @Autowired private NfeService nfeService;

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("notas", nfeService.listarTodas());
        return "fiscal/painel";
    }

    @GetMapping("/emitir/{pedidoId}")
    public String telaEmitir(@PathVariable Long pedidoId, Model model) {
        model.addAttribute("pedidoId", pedidoId);
        model.addAttribute("nfeExistente", nfeService.buscarPorPedido(pedidoId).orElse(null));
        return "fiscal/emitir";
    }

    @PostMapping("/emitir/{pedidoId}")
    public String emitir(@PathVariable Long pedidoId, RedirectAttributes ra) {
        try {
            NfeRegistro nfe = nfeService.emitirNfe(pedidoId);
            if (nfe.getStatus() == NfeRegistro.StatusNfe.AUTORIZADA) {
                ra.addFlashAttribute("sucesso", "NF-e autorizada! Chave: " + nfe.getChaveAcesso());
            } else if (nfe.getStatus() == NfeRegistro.StatusNfe.PROCESSANDO) {
                ra.addFlashAttribute("info", "NF-e enviada — aguardando processamento da SEFAZ. Consulte o status em instantes.");
            } else {
                ra.addFlashAttribute("erro", "Erro: " + nfe.getMensagemSefaz());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/fiscal";
    }

    @PostMapping("/{id}/consultar")
    public String consultar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            NfeRegistro nfe = nfeService.consultarStatus(id);
            ra.addFlashAttribute("sucesso", "Status atualizado: " + nfe.getStatus() + " — " + nfe.getMensagemSefaz());
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/fiscal";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam String justificativa,
                           RedirectAttributes ra) {
        try {
            nfeService.cancelarNfe(id, justificativa);
            ra.addFlashAttribute("sucesso", "NF-e cancelada com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/fiscal";
    }
}
