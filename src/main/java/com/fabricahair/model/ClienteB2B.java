package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "clientes_b2b")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteB2B {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(unique = true, length = 14)
    private String cnpj;

    @Column(name = "inscricao_estadual")
    private String inscricaoEstadual;

    private String telefone;
    private String email;
    private String contato;

    // Endereço completo
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_preco")
    @Builder.Default
    private PerfilPreco perfilPreco = PerfilPreco.VAREJO;

    @Column(name = "limite_credito", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Builder.Default
    private boolean ativo = true;

    public enum PerfilPreco {
        VAREJO, ATACADO, DISTRIBUIDOR
    }
}
