package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transportadoras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transportadora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(unique = true)
    private String cnpj;

    private String telefone;
    private String email;

    @Builder.Default
    private boolean ativo = true;
}
