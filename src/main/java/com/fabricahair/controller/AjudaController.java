package com.fabricahair.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/ajuda")
public class AjudaController {

    @GetMapping
    public String index(Model model) {
        // Renderiza a página principal de ajuda/manual do usuário
        return "ajuda/index";
    }
}
