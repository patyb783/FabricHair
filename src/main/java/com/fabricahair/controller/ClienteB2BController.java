package com.fabricahair.controller;

import com.fabricahair.model.ClienteB2B;
import com.fabricahair.service.ClienteB2BService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/clientes")
public class ClienteB2BController {

    @Autowired private ClienteB2BService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listarTodos());
        return "clientes/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new ClienteB2B());
        model.addAttribute("modoEdicao", false);
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.buscarPorId(id));
        model.addAttribute("modoEdicao", true);
        return "clientes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ClienteB2B cliente, RedirectAttributes ra) {
        try {
            service.salvar(cliente);
            ra.addFlashAttribute("sucesso", "Cliente salvo com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }
        return "redirect:/web/clientes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.excluir(id);
            ra.addFlashAttribute("sucesso", "Cliente removido.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/web/clientes";
    }
}
