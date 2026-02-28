package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nfe_registros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NfeRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoVenda pedido;

    // Identificação Focus NFe
    @Column(name = "referencia_focus", unique = true)
    private String referenciaFocus;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "numero_nfe")
    private String numeroNfe;

    @Column(name = "serie")
    private String serie;

    @Column(name = "status_sefaz")
    private String statusSefaz;

    @Column(name = "mensagem_sefaz", length = 500)
    private String mensagemSefaz;

    @Column(name = "url_danfe", length = 500)
    private String urlDanfe;

    @Column(name = "url_xml", length = 500)
    private String urlXml;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusNfe status = StatusNfe.PENDENTE;

    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "motivo_cancelamento", length = 300)
    private String motivoCancelamento;

    public enum StatusNfe {
        PENDENTE,
        PROCESSANDO,
        AUTORIZADA,
        CANCELADA,
        ERRO,
        DENEGADA
    }
}
