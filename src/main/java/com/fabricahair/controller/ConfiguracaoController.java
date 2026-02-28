package com.fabricahair.controller;

import com.fabricahair.model.ConfiguracaoEmpresa;
import com.fabricahair.service.ConfiguracaoEmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;

@Controller
@RequestMapping("/web/admin/configuracoes")
public class ConfiguracaoController {

    @Autowired
    private ConfiguracaoEmpresaService configuracaoService;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("config", configuracaoService.obterConfiguracaoAtual());
        return "admin/configuracoes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ConfiguracaoEmpresa config,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes ra) {
        try {
            ConfiguracaoEmpresa atual = configuracaoService.obterConfiguracaoAtual();

            // Atualiza os dados de texto
            atual.setRazaoSocial(config.getRazaoSocial());
            atual.setNomeFantasia(config.getNomeFantasia());
            atual.setCnpj(config.getCnpj());
            atual.setTelefone(config.getTelefone());
            atual.setEmail(config.getEmail());
            atual.setEndereco(config.getEndereco());

            // Verifica se uma nova imagem foi enviada
            if (logoFile != null && !logoFile.isEmpty()) {
                if (logoFile.getSize() > 2 * 1024 * 1024) { // 2MB limite
                    throw new RuntimeException("A imagem deve ter no máximo 2MB.");
                }
                String base64Image = Base64.getEncoder().encodeToString(logoFile.getBytes());
                String imageFormato = "data:" + logoFile.getContentType() + ";base64,";
                atual.setLogoBase64(imageFormato + base64Image);
            }

            configuracaoService.salvar(atual);
            ra.addFlashAttribute("sucesso", "Configurações da empresa atualizadas com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar configurações: " + e.getMessage());
        }

        return "redirect:/web/admin/configuracoes";
    }
}
