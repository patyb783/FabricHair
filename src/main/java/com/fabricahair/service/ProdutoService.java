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
        if (produto.getSku() == null || produto.getSku().isBlank()) {
            long newNumber = repository.count() + 1;
            produto.setSku(gerarSku(produto.getNome(), newNumber));
        }
        return repository.save(produto);
    }

    private String gerarSku(String nome, long count) {
        if (nome == null || nome.trim().isEmpty())
            return "SKU-" + String.format("%03d", count);

        String[] partes = nome.trim().split("\\s+");
        StringBuilder prefixo = new StringBuilder();

        if (partes.length >= 1) {
            String p1 = partes[0].replaceAll("[^a-zA-ZÀ-ÿ]", "");
            prefixo.append(p1.length() >= 3 ? p1.substring(0, 3).toUpperCase() : p1.toUpperCase());
        }
        if (partes.length >= 2) {
            String p2 = partes[1].replaceAll("[^a-zA-ZÀ-ÿ]", "");
            if (!p2.isEmpty()) {
                prefixo.append("-").append(p2.length() >= 3 ? p2.substring(0, 3).toUpperCase() : p2.toUpperCase());
            }
        }

        if (prefixo.length() == 0) {
            prefixo.append("SKU");
        }

        return prefixo.toString() + "-" + String.format("%03d", count);
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
