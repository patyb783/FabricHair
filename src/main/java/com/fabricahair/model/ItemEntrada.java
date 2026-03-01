package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_entrada")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "entrada_id", nullable = false)
    private EntradaMercadoria entrada;

    // Distribuidor compra Insumos ou compra Produto Acabado direto para revenda.
    // Para simplificar e unir WMS, vamos permitir Produto Base (polimorfismo do
    // ProdutoBase).
    // ProdutoBase é a classe pai de Insumo e ProdutoAcabado (Table_PER_CLASS ou
    // JOINED).
    // No nosso MVP, Insumo e ProdutoAcabado estao separados sem herança forte de
    // DB.
    // Para resolver, vamos usar a tipagem (TIPO = INSUMO ou ACABADO)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItemEntrada tipoItem;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    @ManyToOne
    @JoinColumn(name = "produto_acabado_id")
    private ProdutoAcabado produtoAcabado;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    // Rastreabilidade FEFO: Quando a entrada vira estoque físico
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lote_gerado_id")
    private Lote loteGerado;

    public enum TipoItemEntrada {
        INSUMO,
        PRODUTO_ACABADO
    }

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (this.quantidade != null && this.valorUnitario != null) {
            this.valorTotal = this.valorUnitario.multiply(new BigDecimal(this.quantidade));
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }
}
