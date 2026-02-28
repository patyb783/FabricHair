package com.fabricahair.repository;

import com.fabricahair.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByInsumoIdOrderByCriadoEmDesc(Long insumoId);
    List<MovimentacaoEstoque> findByProdutoIdOrderByCriadoEmDesc(Long produtoId);
    List<MovimentacaoEstoque> findByOrdemProducaoIdOrderByCriadoEmDesc(Long opId);
}
