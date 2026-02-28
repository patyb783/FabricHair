package com.fabricahair.repository;
import com.fabricahair.model.Fornecedor; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> { List<Fornecedor> findByAtivoTrue(); }
