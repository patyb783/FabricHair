package com.fabricahair.repository;
import com.fabricahair.model.ProdutoAcabado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProdutoAcabadoRepository extends JpaRepository<ProdutoAcabado, Long> {
    List<ProdutoAcabado> findByAtivoTrue();
    List<ProdutoAcabado> findByEstoqueAtualLessThanEqual(Integer estoqueMinimo);
}
