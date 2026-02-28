package com.fabricahair.controller;

import com.fabricahair.model.Usuario;
import com.fabricahair.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/web/admin/usuarios")
public class AdminUsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/usuarios/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/usuarios/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Usuario usuario,
            @RequestParam(value = "novaSenha", required = false) String novaSenha,
            RedirectAttributes ra) {
        try {
            if (usuario.getId() != null) {
                // Editando usuário existente
                Usuario existente = usuarioRepository.findById(usuario.getId())
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

                existente.setUsername(usuario.getUsername());
                existente.setEmail(usuario.getEmail());
                existente.setRole(usuario.getRole());
                existente.setAtivo(usuario.isAtivo());

                // Só atualiza a senha se uma nova foi informada
                if (novaSenha != null && !novaSenha.trim().isEmpty()) {
                    existente.setPassword(passwordEncoder.encode(novaSenha));
                }
                usuarioRepository.save(existente);
                ra.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
            } else {
                // Novo usuário (senha obrigatória)
                if (novaSenha == null || novaSenha.trim().isEmpty()) {
                    throw new RuntimeException("A senha é obrigatória para novos usuários.");
                }
                usuario.setPassword(passwordEncoder.encode(novaSenha));
                usuarioRepository.save(usuario);
                ra.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar usuário: " + e.getMessage());
            if (usuario.getId() == null) {
                return "redirect:/web/admin/usuarios/novo";
            } else {
                return "redirect:/web/admin/usuarios/editar/" + usuario.getId();
            }
        }
        return "redirect:/web/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Usuario> usuarioOp = usuarioRepository.findById(id);
        if (usuarioOp.isPresent()) {
            model.addAttribute("usuario", usuarioOp.get());
            return "admin/usuarios/form";
        }
        ra.addFlashAttribute("erro", "Usuário não encontrado.");
        return "redirect:/web/admin/usuarios";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        // Em vez de excluir fisicamente, vamos desativar por segurança
        Optional<Usuario> usuarioOp = usuarioRepository.findById(id);
        if (usuarioOp.isPresent()) {
            Usuario u = usuarioOp.get();
            // Evitar que o admin se desative
            if ("admin".equals(u.getUsername())) {
                ra.addFlashAttribute("erro", "O usuário administrador principal não pode ser desativado.");
                return "redirect:/web/admin/usuarios";
            }
            u.setAtivo(false);
            usuarioRepository.save(u);
            ra.addFlashAttribute("sucesso", "Usuário desativado com sucesso!");
        } else {
            ra.addFlashAttribute("erro", "Usuário não encontrado.");
        }
        return "redirect:/web/admin/usuarios";
    }
}
