package com.fabricahair.repository;
import com.fabricahair.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    List<Insumo> findByAtivoTrue();
    @Query("SELECT i FROM Insumo i WHERE i.estoqueAtual <= i.estoqueMinimo AND i.ativo = true")
    List<Insumo> findInsumosAbaixoDoMinimo();
}
