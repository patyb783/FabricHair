package com.fabricahair.repository;
import com.fabricahair.model.ClienteB2B; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ClienteB2BRepository extends JpaRepository<ClienteB2B, Long> { List<ClienteB2B> findByAtivoTrue(); }
