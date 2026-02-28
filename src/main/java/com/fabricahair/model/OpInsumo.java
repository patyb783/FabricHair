package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "op_insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_producao_id", nullable = false)
    private OrdemProducao ordemProducao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(name = "quantidade_necessaria", precision = 10, scale = 3)
    private BigDecimal quantidadeNecessaria;

    @Column(name = "quantidade_usada", precision = 10, scale = 3)
    private BigDecimal quantidadeUsada;
}