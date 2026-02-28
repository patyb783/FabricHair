package com.fabricahair.service;

import com.fabricahair.model.ProdutoAcabado;
import com.fabricahair.repository.ProdutoAcabadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoAcabadoRepository repository;

    public List<ProdutoAcabado> listarTodos() {
        return repository.findByAtivoTrue();
    }

    public ProdutoAcabado buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
    }

    public ProdutoAcabado salvar(ProdutoAcabado produto) {
        if (produto.getSku() != null && produto.getSku().isBlank()) {
            produto.setSku(null);
        }
        return repository.save(produto);
    }

    public void excluir(Long id) {
        ProdutoAcabado produto = buscarPorId(id);
        produto.setAtivo(false);
        repository.save(produto); // soft delete
    }

    public List<ProdutoAcabado> listarComEstoqueCritico() {
        return repository.findAll().stream()
            .filter(p -> p.isAtivo() && p.getEstoqueAtual() != null
                && p.getEstoqueMinimo() != null
                && p.getEstoqueAtual() <= p.getEstoqueMinimo())
            .toList();
    }
}
