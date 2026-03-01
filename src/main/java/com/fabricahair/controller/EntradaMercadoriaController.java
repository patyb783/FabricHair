package com.fabricahair.controller;

import com.fabricahair.model.*;
import com.fabricahair.repository.FornecedorRepository;
import com.fabricahair.repository.ItemEntradaRepository;
import com.fabricahair.service.EntradaMercadoriaService;
import com.fabricahair.service.InsumoService;
import com.fabricahair.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/web/entradas")
public class EntradaMercadoriaController {

    @Autowired
    private EntradaMercadoriaService entradaService;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ItemEntradaRepository itemEntradaRepository;

    @Autowired
    private ProdutoService produtoService; // Para Produtos Acabados

    @Autowired
    private InsumoService insumoService;

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("entradas", entradaService.listarTodas());
        return "entradas/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("entrada", new EntradaMercadoria());
        model.addAttribute("fornecedores", fornecedorRepository.findAll());
        return "entradas/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute EntradaMercadoria entrada, RedirectAttributes redirectAttributes) {
        try {
            EntradaMercadoria salva = entradaService.salvarRascunho(entrada);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Rascunho de Entrada nº " + salva.getNumeroNota() + " criado com sucesso! Adicione os itens.");
            return "redirect:/web/entradas/editar/" + salva.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao criar: " + e.getMessage());
            return "redirect:/web/entradas/novo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        EntradaMercadoria entrada = entradaService.buscarPorId(id);
        model.addAttribute("entrada", entrada);
        model.addAttribute("fornecedores", fornecedorRepository.findAll());
        model.addAttribute("novoItem", new ItemEntrada());
        model.addAttribute("insumos", insumoService.listarTodos());
        model.addAttribute("produtosAcabados", produtoService.listarTodos());
        return "entradas/form";
    }

    @PostMapping("/{id}/itens/adicionar")
    public String adicionarItem(@PathVariable Long id,
            @ModelAttribute ItemEntrada novoItem,
            @RequestParam("produtoId") Long produtoId,
            RedirectAttributes redirectAttributes) {
        try {
            EntradaMercadoria entrada = entradaService.buscarPorId(id);
            if (entrada.getStatus() == EntradaMercadoria.StatusEntrada.FINALIZADA) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Esta Entrada já foi fechada.");
                return "redirect:/web/entradas/listar";
            }

            novoItem.setEntrada(entrada);

            // Vincula qual lado da hierarquia foi escolhido (Misto de MVP)
            if (novoItem.getTipoItem() == ItemEntrada.TipoItemEntrada.INSUMO) {
                novoItem.setInsumo(insumoService.buscarPorId(produtoId));
            } else {
                novoItem.setProdutoAcabado(produtoService.buscarPorId(produtoId));
            }
            novoItem.calcularTotal();
            itemEntradaRepository.save(novoItem);

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Item adicionado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro: " + e.getMessage());
        }
        return "redirect:/web/entradas/editar/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id,
            @RequestParam(value = "gerarFinanceiro", required = false) boolean gerarFinanceiro,
            @RequestParam(value = "diasVencimento", defaultValue = "30", required = false) int diasVencimento,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate dataVen = gerarFinanceiro ? LocalDate.now().plusDays(diasVencimento) : null;
            EntradaMercadoria entradaFinal = entradaService.finalizarEntrada(id, dataVen);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Entrada Fiscal/Estoque consolidada. Total Recebido: R$ " + entradaFinal.getValorTotal());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro Crítico ao Integrar Estoque: " + e.getMessage());
        }
        return "redirect:/web/entradas/listar";
    }
}
