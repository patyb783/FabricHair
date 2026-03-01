package com.fabricahair.controller;

import com.fabricahair.model.RegistroPonto;
import com.fabricahair.model.Usuario;
import com.fabricahair.repository.RegistroPontoRepository;
import com.fabricahair.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/web/rh")
public class PontoController {

    @Autowired
    private RegistroPontoRepository pontoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Tela de Bater Ponto para o Operário comum (Mobile Friendly)
    @GetMapping("/ponto")
    public String telaPonto(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/web/auth/login";
        }
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        if (u != null) {
            RegistroPonto registroHoje = pontoRepository.findByUsuarioIdAndDataRegistro(u.getId(), LocalDate.now())
                    .orElse(new RegistroPonto());
            model.addAttribute("registro", registroHoje);
        }
        return "rh/ponto";
    }

    @PostMapping("/ponto/registrar")
    public String baterPonto(@RequestParam String tipoAcao, Authentication auth, RedirectAttributes ra) {
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        RegistroPonto registro = pontoRepository.findByUsuarioIdAndDataRegistro(u.getId(), hoje)
                .orElse(RegistroPonto.builder()
                        .usuario(u)
                        .dataRegistro(hoje)
                        .build());

        try {
            switch (tipoAcao) {
                case "ENTRADA":
                    if (registro.getEntrada() != null)
                        throw new RuntimeException("Entrada já registrada hoje.");
                    registro.setEntrada(agora);
                    break;
                case "INICIO_ALMOCO":
                    if (registro.getEntrada() == null)
                        throw new RuntimeException("Você precisa bater a Entrada primeiro.");
                    if (registro.getInicioAlmoco() != null)
                        throw new RuntimeException("Início do almoço já registrado.");
                    registro.setInicioAlmoco(agora);
                    break;
                case "FIM_ALMOCO":
                    if (registro.getInicioAlmoco() == null)
                        throw new RuntimeException("Você precisa registrar a saída para o almoço primeiro.");
                    if (registro.getFimAlmoco() != null)
                        throw new RuntimeException("Retorno do almoço já registrado.");
                    registro.setFimAlmoco(agora);
                    break;
                case "SAIDA":
                    if (registro.getEntrada() == null)
                        throw new RuntimeException("Nenhuma Entrada registrada para fechar o dia.");
                    if (registro.getSaida() != null)
                        throw new RuntimeException("Saída já registrada hoje.");
                    registro.setSaida(agora);
                    break;
            }
            pontoRepository.save(registro);
            ra.addFlashAttribute("sucesso", "Ponto registrado com sucesso: " + tipoAcao);
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/web/rh/ponto";
    }

    // Tela Gerencial (Admin/Gerentes)
    @GetMapping("/gestao")
    public String gestaoFrequencia(Model model) {
        model.addAttribute("registrosHoje", pontoRepository.findByDataRegistroOrderByEntradaDesc(LocalDate.now()));
        return "rh/gestao";
    }
}
