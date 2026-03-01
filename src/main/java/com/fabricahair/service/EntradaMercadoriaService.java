package com.fabricahair.service;

import com.fabricahair.model.*;
import com.fabricahair.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private MovimentacaoEstoqueRepository movimentacaoRepository;

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

            // Cria o Lote Fisico usando nomenclatura do DB
            Lote novoLote = Lote.builder()
                    .numeroLote("NF" + entrada.getNumeroNota() + "-ID" + item.getId())
                    .dataFabricacao(LocalDate.now())
                    .dataValidade(LocalDate.now().plusYears(2))
                    .quantidade(new BigDecimal(item.getQuantidade()))
                    .quantidadeDisponivel(new BigDecimal(item.getQuantidade()))
                    .status(Lote.StatusLote.APROVADO)
                    .criadoEm(LocalDateTime.now())
                    .dataLiberacao(LocalDateTime.now())
                    .analistaQualidade("NF Fornecedor")
                    .build();

            // Sendo distribuidor MVP, a revenda entra como Produto Acabado
            if (item.getTipoItem() == ItemEntrada.TipoItemEntrada.PRODUTO_ACABADO) {
                novoLote.setProduto(item.getProdutoAcabado());
                novoLote.setLocalizacao(item.getProdutoAcabado().getLocalizacaoPadrao());
            } else {
                throw new IllegalStateException(
                        "Para módulo Distribuidor, o Inbound Logístico exige cadastro de revenda sob Estoque > Produtos Acabados.");
            }

            Lote loteSalvo = loteRepository.save(novoLote);
            item.setLoteGerado(loteSalvo);
            itemEntradaRepository.save(item);

            MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                    .produtoId(item.getProdutoAcabado().getId())
                    .quantidade(new BigDecimal(item.getQuantidade()))
                    .tipo(MovimentacaoEstoque.TipoMovimentacao.ENTRADA_COMPRA)
                    .descricao("Entrada NF Compra: " + entrada.getNumeroNota())
                    .loteId(loteSalvo.getId())
                    .criadoEm(LocalDateTime.now())
                    .build();
            movimentacaoRepository.save(mov);
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
