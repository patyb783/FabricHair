package com.fabricahair.repository;

import com.fabricahair.model.Lote;
import com.fabricahair.model.Lote.StatusLote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByStatus(StatusLote status);
    List<Lote> findByProdutoIdAndStatus(Long produtoId, StatusLote status);
}
