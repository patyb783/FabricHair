package com.fabricahair.controller;

import com.fabricahair.model.*;
import com.fabricahair.repository.*;
import com.fabricahair.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web/estoque/produtos")
public class ProdutoController {

    @Autowired private ProdutoService produtoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "produtos/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new ProdutoAcabado());
        model.addAttribute("modoEdicao", false);
        return "produtos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("produto", produtoService.buscarPorId(id));
        model.addAttribute("modoEdicao", true);
        return "produtos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ProdutoAcabado produto, RedirectAttributes ra) {
        try {
            produtoService.salvar(produto);
            ra.addFlashAttribute("sucesso", "Produto salvo com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar produto: " + e.getMessage());
        }
        return "redirect:/web/estoque/produtos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            produtoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Produto excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/web/estoque/produtos";
    }
}
