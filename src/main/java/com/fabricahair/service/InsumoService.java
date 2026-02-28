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
        return repository.save(insumo);
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
