package com.fabricahair.repository;

import com.fabricahair.model.TituloFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TituloFinanceiroRepository extends JpaRepository<TituloFinanceiro, Long> {

    List<TituloFinanceiro> findByTipoOrderByDataVencimentoAsc(TituloFinanceiro.TipoTitulo tipo);

    List<TituloFinanceiro> findByTipoAndStatusOrderByDataVencimentoAsc(TituloFinanceiro.TipoTitulo tipo,
            TituloFinanceiro.StatusTitulo status);

    List<TituloFinanceiro> findByPedidoVendaId(Long pedidoId);
}
