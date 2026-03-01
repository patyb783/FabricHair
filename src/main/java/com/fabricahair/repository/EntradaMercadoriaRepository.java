package com.fabricahair.repository;

import com.fabricahair.model.EntradaMercadoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaMercadoriaRepository extends JpaRepository<EntradaMercadoria, Long> {
}
