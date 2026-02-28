package com.fabricahair.controller;

import com.fabricahair.model.Transportadora;
import com.fabricahair.repository.TransportadoraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/logistica/transportadoras")
public class TransportadoraController {

    @Autowired
    private TransportadoraRepository transportadoraRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("transportadoras", transportadoraRepository.findAll());
        return "logistica/transportadoras/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("transportadora", new Transportadora());
        return "logistica/transportadoras/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Transportadora transportadora, RedirectAttributes ra) {
        try {
            transportadoraRepository.save(transportadora);
            ra.addFlashAttribute("sucesso", "Transportadora salva com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }
        return "redirect:/web/logistica/transportadoras";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Transportadora t = transportadoraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("transportadora", t);
        return "logistica/transportadoras/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            transportadoraRepository.deleteById(id);
            ra.addFlashAttribute("sucesso", "Transportadora removida.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao remover (pode estar em uso): " + e.getMessage());
        }
        return "redirect:/web/logistica/transportadoras";
    }
}
