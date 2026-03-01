package com.fabricahair.service;

import com.fabricahair.model.*;
import com.fabricahair.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EntradaMercadoriaService {

    @Autowired
    private EntradaMercadoriaRepository entradaRepository;

    @Autowired
    private ItemEntradaRepository itemEntradaRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimentacaoEstoqueService movimentacaoService;

    @Autowired
    private TituloFinanceiroRepository tituloFinanceiroRepository;

    public List<EntradaMercadoria> listarTodas() {
        return entradaRepository.findAll();
    }

    public EntradaMercadoria buscarPorId(Long id) {
        return entradaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Entrada não encontrada"));
    }

    @Transactional
    public EntradaMercadoria salvarRascunho(EntradaMercadoria entrada) {
        // Inicializa o total se não vier
        if (entrada.getValorTotal() == null)
            entrada.setValorTotal(BigDecimal.ZERO);
        return entradaRepository.save(entrada);
    }

    @Transactional
    public EntradaMercadoria finalizarEntrada(Long id, LocalDate dataVencimentoPagamento) {
        EntradaMercadoria entrada = buscarPorId(id);

        if (entrada.getStatus() == EntradaMercadoria.StatusEntrada.FINALIZADA) {
            throw new IllegalStateException("Esta Entrada já foi finalizada e processada.");
        }

        // 1. Processar itens, gerar LOTES e dar Entrada no Estoque
        BigDecimal totalEntrada = BigDecimal.ZERO;

        for (ItemEntrada item : itemEntradaRepository.findByEntradaId(entrada.getId())) {

            // Recalcula total do item por garantia
            item.calcularTotal();
            totalEntrada = totalEntrada.add(item.getValorTotal());

            // Cria o Lote Fisico
            Lote novoLote = Lote.builder()
                    .codigo("NF" + entrada.getNumeroNota() + "-ID" + item.getId())
                    .dataFabricacao(LocalDate.now())
                    // Padrão de 2 anos de validade para revenda se não informado
                    .dataValidade(LocalDate.now().plusYears(2))
                    .quantidadeInicial(item.getQuantidade())
                    .quantidadeAtual(item.getQuantidade())
                    // Como é compra de terceiro, já nasce APROVADO
                    .aprovado(true)
                    .build();

            // Preenche de acordo com o Tipo
            ProdutoBase refProduto;
            if (item.getTipoItem() == ItemEntrada.TipoItemEntrada.INSUMO) {
                novoLote.setInsumo(item.getInsumo());
                novoLote.setLocalizacao(item.getInsumo().getLocalizacaoPadrao());
                refProduto = item.getInsumo();
            } else {
                novoLote.setProdutoAcabado(item.getProdutoAcabado());
                novoLote.setLocalizacao(item.getProdutoAcabado().getLocalizacaoPadrao());
                refProduto = item.getProdutoAcabado();
            }

            Lote loteSalvo = loteRepository.save(novoLote);
            item.setLoteGerado(loteSalvo);
            itemEntradaRepository.save(item);

            // Grava histórico de movimentação Positiva
            movimentacaoService.registrarMovimentacao(
                    refProduto,
                    item.getQuantidade(),
                    MovimentacaoEstoque.TipoMovimentacao.ENTRADA,
                    "Entrada via NF de Compra Fornecedor Nº " + entrada.getNumeroNota());
        }

        entrada.setValorTotal(totalEntrada);
        entrada.setStatus(EntradaMercadoria.StatusEntrada.FINALIZADA);

        // 2. Integração Financeira Automática
        if (dataVencimentoPagamento != null) {
            TituloFinanceiro tituloPagar = TituloFinanceiro.builder()
                    .tipo(TituloFinanceiro.TipoTitulo.PAGAR)
                    .descricao("Compra Fornecedor " + entrada.getFornecedor().getRazaoSocial() + " NF: "
                            + entrada.getNumeroNota())
                    .valorOriginal(totalEntrada)
                    .dataVencimento(dataVencimentoPagamento)
                    .status(TituloFinanceiro.StatusTitulo.PENDENTE)
                    .build();
            tituloFinanceiroRepository.save(tituloPagar);
        }

        return entradaRepository.save(entrada);
    }
}
