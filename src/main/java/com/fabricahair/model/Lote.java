package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoAcabado produto;

    @Column(name = "numero_lote", nullable = false, unique = true)
    private String numeroLote;

    @Column(name = "data_fabricacao")
    private LocalDate dataFabricacao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(precision = 10, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "quantidade_disponivel", precision = 10, scale = 3)
    private BigDecimal quantidadeDisponivel;

    @Enumerated(EnumType.STRING)
    private StatusLote status = StatusLote.EM_QUARENTENA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_producao_id")
    private OrdemProducao ordemProducao;

    @Column(name = "analista_qualidade")
    private String analistaQualidade;

    @Column(name = "data_liberacao")
    private LocalDateTime dataLiberacao;

    @Column(name = "criado_em")
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum StatusLote {
        EM_QUARENTENA, APROVADO, REPROVADO, VENDIDO, VENCIDO
    }
}
