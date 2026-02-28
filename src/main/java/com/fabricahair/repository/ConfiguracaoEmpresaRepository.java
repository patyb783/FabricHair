package com.fabricahair.repository;

import com.fabricahair.model.ConfiguracaoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracaoEmpresaRepository extends JpaRepository<ConfiguracaoEmpresa, Long> {
}
