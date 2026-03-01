package com.fabricahair.repository;

import com.fabricahair.model.LogIot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogIotRepository extends JpaRepository<LogIot, Long> {

    List<LogIot> findByDispositivoIdOrderByDataHoraDesc(Long dispositivoId);

    List<LogIot> findByDispositivoIdAndDataHoraBetween(Long dispositivoId, LocalDateTime inicio, LocalDateTime fim);
}
