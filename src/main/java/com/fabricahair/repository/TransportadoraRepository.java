package com.fabricahair.repository;

import com.fabricahair.model.Transportadora;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransportadoraRepository extends JpaRepository<Transportadora, Long> {
    List<Transportadora> findByAtivoTrue();
}
