package com.fabricahair.repository;

import com.fabricahair.model.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    Optional<RegistroPonto> findByUsuarioIdAndDataRegistro(Long usuarioId, LocalDate dataRegistro);

    List<RegistroPonto> findByDataRegistroOrderByEntradaDesc(LocalDate dataRegistro);

    List<RegistroPonto> findByUsuarioIdOrderByDataRegistroDesc(Long usuarioId);
}
