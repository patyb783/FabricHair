package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordens_producao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_op", unique = true)
    private String numeroOp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoAcabado produto;

    @Column(name = "quantidade_planejada", precision = 10, scale = 3)
    private BigDecimal quantidadePlanejada;

    @Column(name = "quantidade_produzida", precision = 10, scale = 3)
    private BigDecimal quantidadeProduzida;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusOP status = StatusOP.PLANEJADA;

    private String observacoes;

    @Column(name = "quantidade_refugo", precision = 10, scale = 3)
    private BigDecimal quantidadeRefugo;

    @Column(name = "motivo_refugo")
    private String motivoRefugo;

    @Column(name = "custo_insumos", precision = 12, scale = 2)
    private BigDecimal custoInsumos;

    @Column(name = "custo_mao_de_obra", precision = 12, scale = 2)
    private BigDecimal custoMaoDeObra;

    @Column(name = "custo_energia", precision = 12, scale = 2)
    private BigDecimal custoEnergia;

    @Column(name = "custo_total", precision = 12, scale = 2)
    private BigDecimal custoTotal;

    @Column(name = "custo_unitario", precision = 12, scale = 2)
    private BigDecimal custoUnitario;

    @OneToMany(mappedBy = "ordemProducao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpInsumo> insumos;

    @OneToMany(mappedBy = "ordemProducao")
    private List<Lote> lotes;

    public enum StatusOP {
        PLANEJADA, EM_PRODUCAO, FINALIZADA, CANCELADA
    }
}
