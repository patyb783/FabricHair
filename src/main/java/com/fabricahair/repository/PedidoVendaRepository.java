package com.fabricahair.repository;
import com.fabricahair.model.PedidoVenda; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface PedidoVendaRepository extends JpaRepository<PedidoVenda, Long> {
    List<PedidoVenda> findByStatus(PedidoVenda.StatusPedido status);
}
