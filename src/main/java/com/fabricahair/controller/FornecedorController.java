package com.fabricahair.controller;

import com.fabricahair.model.Fornecedor;
import com.fabricahair.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/fornecedores")
public class FornecedorController {

    @Autowired private FornecedorService fornecedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        return "fornecedores/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        model.addAttribute("modoEdicao", false);
        return "fornecedores/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.buscarPorId(id));
        model.addAttribute("modoEdicao", true);
        return "fornecedores/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor, RedirectAttributes ra) {
        try {
            fornecedorService.salvar(fornecedor);
            ra.addFlashAttribute("sucesso", "Fornecedor salvo com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }
        return "redirect:/web/fornecedores";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            fornecedorService.excluir(id);
            ra.addFlashAttribute("sucesso", "Fornecedor removido.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/web/fornecedores";
    }
}
