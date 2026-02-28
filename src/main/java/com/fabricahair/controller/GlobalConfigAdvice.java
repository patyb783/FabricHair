package com.fabricahair.controller;

import com.fabricahair.model.ConfiguracaoEmpresa;
import com.fabricahair.service.ConfiguracaoEmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalConfigAdvice {

    @Autowired
    private ConfiguracaoEmpresaService configuracaoEmpresaService;

    @ModelAttribute("empresaInfo")
    public ConfiguracaoEmpresa populateEmpresaInfo() {
        return configuracaoEmpresaService.obterConfiguracaoAtual();
    }
}
