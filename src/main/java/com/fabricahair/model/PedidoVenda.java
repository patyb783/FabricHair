package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos_venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteB2B cliente;

    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusPedido status = StatusPedido.RASCUNHO;

    @Column(name = "valor_total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "nfe_chave_acesso", length = 44)
    private String nfeChaveAcesso;

    @Column(name = "nfe_status")
    private String nfeStatus;

    // Campos de Logística e Distribuição (Fase 3)
    @Column(name = "data_previsao_entrega")
    private LocalDate dataPrevisaoEntrega;

    @Column(name = "data_faturamento")
    private LocalDateTime dataFaturamento;

    @Column(name = "data_entrega_realizada")
    private LocalDateTime dataEntregaRealizada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportadora_id")
    private Transportadora transportadora;

    @Column(name = "codigo_rastreio")
    private String codigoRastreio;

    @Column(name = "valor_frete", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "otif_avaliacao", length = 50)
    private String otifAvaliacao; // e.g. "NO PRAZO", "ATRASADO"

    private String observacoes;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens;

    public enum StatusPedido {
        RASCUNHO, CONFIRMADO, FATURADO, ENTREGUE, CANCELADO
    }
}
