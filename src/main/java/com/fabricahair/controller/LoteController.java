package com.fabricahair.controller;

import com.fabricahair.model.Lote;
import com.fabricahair.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/web/producao/lotes")
public class LoteController {

    @Autowired
    private LoteRepository loteRepository;

    @GetMapping
    public String listar(Model model) {
        List<Lote> lotes = loteRepository.findAll();
        // Em um sistema real, faríamos paginação
        lotes.sort((l1, l2) -> l2.getCriadoEm().compareTo(l1.getCriadoEm()));
        model.addAttribute("lotes", lotes);
        return "lotes/listar";
    }
}
