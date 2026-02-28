package com.fabricahair.service;

import com.fabricahair.model.ConfiguracaoEmpresa;
import com.fabricahair.repository.ConfiguracaoEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracaoEmpresaService {

    @Autowired
    private ConfiguracaoEmpresaRepository repository;

    public ConfiguracaoEmpresa obterConfiguracaoAtual() {
        return repository.findById(1L).orElseGet(this::criarConfiguracaoPadrao);
    }

    private ConfiguracaoEmpresa criarConfiguracaoPadrao() {
        ConfiguracaoEmpresa padrao = ConfiguracaoEmpresa.builder()
                .id(1L)
                .razaoSocial("FabricHair ERP")
                .nomeFantasia("FabricHair ERP")
                .cnpj("00.000.000/0001-00")
                .build();
        return repository.save(padrao);
    }

    public ConfiguracaoEmpresa salvar(ConfiguracaoEmpresa config) {
        config.setId(1L); // Force ID 1
        return repository.save(config);
    }
}
