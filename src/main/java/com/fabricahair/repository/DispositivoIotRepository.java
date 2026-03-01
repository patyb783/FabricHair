package com.fabricahair.repository;

import com.fabricahair.model.DispositivoIot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoIotRepository extends JpaRepository<DispositivoIot, Long> {

    Optional<DispositivoIot> findByMacAddress(String macAddress);

    List<DispositivoIot> findByTipoSensor(DispositivoIot.TipoSensor tipoSensor);

    List<DispositivoIot> findByStatus(DispositivoIot.StatusSensor status);
}
