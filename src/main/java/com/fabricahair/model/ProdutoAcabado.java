package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "produtos_acabados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoAcabado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true)
    private String sku;

    @Column(length = 8)
    private String ncm; // Código NCM (8 dígitos) para NF-e

    private String ean; // Código de barras EAN-13

    @Column(name = "registro_anvisa")
    private String registroAnvisa;

    private String unidade; // UN, KG, L, ML, etc.

    @Column(name = "preco_custo", precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda_varejo", precision = 10, scale = 2)
    private BigDecimal precoVendaVarejo;

    @Column(name = "preco_venda_atacado", precision = 10, scale = 2)
    private BigDecimal precoVendaAtacado;

    @Column(name = "preco_venda_distribuidor", precision = 10, scale = 2)
    private BigDecimal precoVendaDistribuidor;

    @Column(name = "estoque_atual")
    private Integer estoqueAtual = 0;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo = 0;

    private String descricao;

    @Column(name = "localizacao_padrao", length = 100)
    private String localizacaoPadrao;
    private boolean ativo = true;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL)
    private List<Lote> lotes;
}
