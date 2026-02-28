package com.fabricahair.service;

import com.fabricahair.model.ClienteB2B;
import com.fabricahair.repository.ClienteB2BRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClienteB2BService {

    @Autowired
    private ClienteB2BRepository repository;

    public List<ClienteB2B> listarTodos() {
        return repository.findByAtivoTrue();
    }

    public ClienteB2B buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + id));
    }

    public ClienteB2B salvar(ClienteB2B cliente) {
        if (cliente.getCnpj() != null) {
            cliente.setCnpj(cliente.getCnpj().replaceAll("[^0-9]", ""));
        }
        if (cliente.getCep() != null) {
            cliente.setCep(cliente.getCep().replaceAll("[^0-9]", ""));
        }
        return repository.save(cliente);
    }

    public void excluir(Long id) {
        ClienteB2B c = buscarPorId(id);
        c.setAtivo(false);
        repository.save(c);
    }
}
