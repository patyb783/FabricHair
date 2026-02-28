package com.fabricahair.controller;

import com.fabricahair.model.MovimentacaoEstoque;
import com.fabricahair.repository.MovimentacaoEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/web/estoque/movimentacoes")
public class MovimentacaoEstoqueController {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @GetMapping
    public String listar(Model model) {
        List<MovimentacaoEstoque> movimentacoes = movimentacaoRepository.findAll();
        // Em um sistema real, faríamos paginação e ordenação descrescente por data
        // Para simplificar no protótipo, vamos inverter a lista para mostrar as mais
        // recentes primeiro
        movimentacoes.sort((m1, m2) -> m2.getCriadoEm().compareTo(m1.getCriadoEm()));
        model.addAttribute("movimentacoes", movimentacoes);
        return "movimentacoes/listar";
    }
}
