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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoIntegracao tipoIntegracao = TipoIntegracao.NENHUM;

    private String nomeIntegracao;
    private String urlRastreioPadrao;
    private Integer prazoEntregaPadrao;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal taxaValorProduto;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal taxaAdicionalPedido;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal taxaValorFrete;

    @Builder.Default
    private boolean permiteColeta = false;

    // Campos específicos para API da Loggi
    private String loggiEmail;
    private String loggiApiKey;

    @Builder.Default
    private boolean ativo = true;

    public enum TipoIntegracao {
        NENHUM, LOGGI, CORREIOS, JADLOG, GETLOG, OUTROS
    }
}
