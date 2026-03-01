package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantidade;

    private String descricao;

    // Referências (pode ser insumo ou produto)
    @Column(name = "insumo_id")
    private Long insumoId;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "ordem_producao_id")
    private Long ordemProducaoId;

    @Column(name = "lote_id")
    private Long loteId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoMovimentacao {
        ENTRADA_COMPRA, // compra de terceiros (Distribuidor ou NF)
        ENTRADA_INSUMO, // compra de insumo
        SAIDA_INSUMO_OP, // insumo consumido em OP
        ENTRADA_PRODUTO_OP, // produto gerado por OP
        SAIDA_PRODUTO_VENDA, // produto vendido
        AJUSTE_POSITIVO,
        AJUSTE_NEGATIVO
    }
}
