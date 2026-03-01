package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entradas_mercadoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaMercadoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private String numeroNota;

    @Column(nullable = false)
    private LocalDate dataEmissao;

    @CreationTimestamp
    private LocalDateTime dataRegistro;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEntrada status = StatusEntrada.RASCUNHO;

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemEntrada> itens = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public enum StatusEntrada {
        RASCUNHO,
        FINALIZADA, // Estoque abatido, titulo financeiro gerado
        CANCELADA
    }
}
