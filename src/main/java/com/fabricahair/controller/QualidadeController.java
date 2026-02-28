package com.fabricahair.controller;

import com.fabricahair.service.QualidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/web/qualidade")
public class QualidadeController {

    @Autowired
    private QualidadeService qualidadeService;

    @GetMapping("/quarentena")
    public String quarentena(Model model) {
        model.addAttribute("lotes", qualidadeService.listarEmQuarentena());
        return "qualidade/quarentena";
    }

    @PostMapping("/aprovar/{id}")
    public String aprovar(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            String analista = principal != null ? principal.getName() : "Sistema";
            qualidadeService.aprovarLote(id, analista);
            ra.addFlashAttribute("sucesso", "Lote aprovado com sucesso! Estoque do produto atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao aprovar lote: " + e.getMessage());
        }
        return "redirect:/web/qualidade/quarentena";
    }

    @PostMapping("/reprovar/{id}")
    public String reprovar(@PathVariable Long id, @RequestParam String justificativa, Principal principal,
            RedirectAttributes ra) {
        try {
            String analista = principal != null ? principal.getName() : "Sistema";
            qualidadeService.reprovarLote(id, analista, justificativa);
            ra.addFlashAttribute("sucesso", "Lote reprovado com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao reprovar lote: " + e.getMessage());
        }
        return "redirect:/web/qualidade/quarentena";
    }
}
