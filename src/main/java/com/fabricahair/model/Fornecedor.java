package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "fornecedores")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Fornecedor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(unique = true, length = 14)
    private String cnpj;

    private String contato;
    private String telefone;
    private String email;
    private String endereco;
    private String cidade;
    private String estado;
    private boolean ativo = true;
}
