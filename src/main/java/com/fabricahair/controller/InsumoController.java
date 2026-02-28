package com.fabricahair.controller;

import com.fabricahair.model.Fornecedor;
import com.fabricahair.model.Insumo;
import com.fabricahair.service.FornecedorService;
import com.fabricahair.service.InsumoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/estoque/insumos")
public class InsumoController {

    @Autowired private InsumoService insumoService;
    @Autowired private FornecedorService fornecedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("insumos", insumoService.listarTodos());
        model.addAttribute("alertas", insumoService.listarAbaixoDoMinimo().size());
        return "insumos/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("insumo", new Insumo());
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        model.addAttribute("modoEdicao", false);
        return "insumos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("insumo", insumoService.buscarPorId(id));
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        model.addAttribute("modoEdicao", true);
        return "insumos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Insumo insumo,
                         @RequestParam(required = false) Long fornecedorId,
                         RedirectAttributes ra) {
        try {
            if (fornecedorId != null) {
                insumo.setFornecedor(fornecedorService.buscarPorId(fornecedorId));
            }
            insumoService.salvar(insumo);
            ra.addFlashAttribute("sucesso", "Insumo salvo com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }
        return "redirect:/web/estoque/insumos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            insumoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Insumo removido.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/web/estoque/insumos";
    }
}
