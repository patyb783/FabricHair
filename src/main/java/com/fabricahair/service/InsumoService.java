package com.fabricahair.service;

import com.fabricahair.model.Insumo;
import com.fabricahair.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InsumoService {

    @Autowired
    private InsumoRepository repository;

    public List<Insumo> listarTodos() {
        return repository.findByAtivoTrue();
    }

    public Insumo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo não encontrado: " + id));
    }

    public Insumo salvar(Insumo insumo) {
        if (insumo.getSku() == null || insumo.getSku().isBlank()) {
            long newNumber = repository.count() + 1;
            insumo.setSku(gerarSku(insumo.getNome(), newNumber));
        }
        return repository.save(insumo);
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
        Insumo insumo = buscarPorId(id);
        insumo.setAtivo(false);
        repository.save(insumo);
    }

    public List<Insumo> listarAbaixoDoMinimo() {
        return repository.findInsumosAbaixoDoMinimo();
    }
}
