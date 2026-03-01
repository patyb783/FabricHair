package com.fabricahair.repository;

import com.fabricahair.model.ItemEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemEntradaRepository extends JpaRepository<ItemEntrada, Long> {
    List<ItemEntrada> findByEntradaId(Long entradaId);
}
