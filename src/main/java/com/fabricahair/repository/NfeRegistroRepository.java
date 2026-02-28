package com.fabricahair.repository;

import com.fabricahair.model.NfeRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NfeRegistroRepository extends JpaRepository<NfeRegistro, Long> {
    Optional<NfeRegistro> findByPedidoId(Long pedidoId);
    Optional<NfeRegistro> findByReferenciaFocus(String referencia);
    List<NfeRegistro> findAllByOrderByCriadoEmDesc();
}
