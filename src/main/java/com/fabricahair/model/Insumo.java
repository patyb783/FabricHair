package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true)
    private String sku;

    private String unidade;
    private String descricao;

    @Column(name = "estoque_atual", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    @Column(name = "preco_unitario", precision = 10, scale = 4)
    private BigDecimal precoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Builder.Default
    private boolean ativo = true;
}
