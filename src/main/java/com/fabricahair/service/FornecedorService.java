package com.fabricahair.service;

import com.fabricahair.model.Fornecedor;
import com.fabricahair.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository repository;

    public List<Fornecedor> listarTodos() {
        return repository.findByAtivoTrue();
    }

    public Fornecedor buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado: " + id));
    }

    public Fornecedor salvar(Fornecedor fornecedor) {
        if (fornecedor.getCnpj() != null) {
            fornecedor.setCnpj(fornecedor.getCnpj().replaceAll("[^0-9]", ""));
        }
        return repository.save(fornecedor);
    }

    public void excluir(Long id) {
        Fornecedor f = buscarPorId(id);
        f.setAtivo(false);
        repository.save(f);
    }
}
