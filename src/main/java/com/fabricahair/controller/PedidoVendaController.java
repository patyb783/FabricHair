package com.fabricahair.controller;

import com.fabricahair.model.PedidoVenda;
import com.fabricahair.repository.ProdutoAcabadoRepository;
import com.fabricahair.service.ClienteB2BService;
import com.fabricahair.service.PedidoVendaService;
import com.fabricahair.repository.TransportadoraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/web/pedidos")
public class PedidoVendaController {

    @Autowired
    private PedidoVendaService pedidoService;
    @Autowired
    private ClienteB2BService clienteService;
    @Autowired
    private ProdutoAcabadoRepository produtoRepository;
    @Autowired
    private TransportadoraRepository transportadoraRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "pedidos/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("produtos", produtoRepository.findByAtivoTrue());
        model.addAttribute("transportadoras", transportadoraRepository.findByAtivoTrue());
        return "pedidos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Long clienteId,
            @RequestParam(required = false) String observacoes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPrevisaoEntrega,
            @RequestParam(required = false) Long transportadoraId,
            @RequestParam(required = false) BigDecimal valorFrete,
            @RequestParam(required = false) List<Long> produtoIds,
            @RequestParam(required = false) List<BigDecimal> quantidades,
            RedirectAttributes ra) {
        try {
            PedidoVenda p = pedidoService.criar(clienteId, observacoes, produtoIds, quantidades,
                    dataPrevisaoEntrega, transportadoraId, valorFrete);
            ra.addFlashAttribute("sucesso", "Pedido " + p.getNumeroPedido() + " criado!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao criar pedido: " + e.getMessage());
        }
        return "redirect:/web/pedidos";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.buscarPorId(id));
        return "pedidos/detalhe";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.confirmar(id);
            ra.addFlashAttribute("sucesso", "Pedido confirmado!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    @PostMapping("/{id}/faturar")
    public String faturar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.faturar(id);
            ra.addFlashAttribute("sucesso", "Pedido faturado! Estoque de produtos atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    @PostMapping("/{id}/entregar")
    public String entregar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.marcarEntregue(id);
            ra.addFlashAttribute("sucesso", "Pedido marcado como entregue!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.cancelar(id);
            ra.addFlashAttribute("sucesso", "Pedido cancelado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/pedidos";
    }
}
