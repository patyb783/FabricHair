package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "titulos_financeiros")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TituloFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTitulo tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorOriginal;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorPago;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusTitulo status = StatusTitulo.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_venda_id")
    private PedidoVenda pedidoVenda;

    // We can also have a foreign key to "Fornecedor" later for Contas a Pagar.

    public enum TipoTitulo {
        PAGAR,
        RECEBER
    }

    public enum StatusTitulo {
        PENDENTE,
        PAGO,
        ATRASADO,
        CANCELADO
    }
}
