package com.fabricahair.repository;
import com.fabricahair.model.OrdemProducao; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface OrdemProducaoRepository extends JpaRepository<OrdemProducao, Long> {
    List<OrdemProducao> findByStatusIn(List<OrdemProducao.StatusOP> statuses);
}
