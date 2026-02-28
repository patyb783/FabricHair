package com.fabricahair.service;

import com.fabricahair.model.*;
import com.fabricahair.model.PedidoVenda.StatusPedido;
import com.fabricahair.model.MovimentacaoEstoque.TipoMovimentacao;
import com.fabricahair.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PedidoVendaService {

    @Autowired
    private PedidoVendaRepository pedidoRepository;
    @Autowired
    private ClienteB2BRepository clienteRepository;
    @Autowired
    private ProdutoAcabadoRepository produtoRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired
    private TransportadoraRepository transportadoraRepository;

    public List<PedidoVenda> listarTodos() {
        return pedidoRepository.findAll();
    }

    public PedidoVenda buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
    }

    @Transactional
    public PedidoVenda criar(Long clienteId, String observacoes,
            List<Long> produtoIds, List<BigDecimal> quantidades,
            LocalDate dataPrevisaoEntrega, Long transportadoraId, BigDecimal valorFrete) {
        ClienteB2B cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        if (produtoIds == null || produtoIds.isEmpty()) {
            throw new RuntimeException("O pedido deve conter pelo menos um produto.");
        }

        Transportadora transportadora = null;
        if (transportadoraId != null) {
            transportadora = transportadoraRepository.findById(transportadoraId)
                    .orElse(null);
        }

        String numeroPedido = gerarNumeroPedido();

        PedidoVenda pedido = PedidoVenda.builder()
                .numeroPedido(numeroPedido)
                .cliente(cliente)
                .status(StatusPedido.RASCUNHO)
                .criadoEm(LocalDateTime.now())
                .observacoes(observacoes)
                .dataPrevisaoEntrega(dataPrevisaoEntrega)
                .transportadora(transportadora)
                .valorFrete(valorFrete != null ? valorFrete : BigDecimal.ZERO)
                .itens(new ArrayList<>())
                .build();

        BigDecimal totalPedido = BigDecimal.ZERO;

        if (produtoIds != null) {
            for (int i = 0; i < produtoIds.size(); i++) {
                if (produtoIds.get(i) == null)
                    continue;
                ProdutoAcabado produto = produtoRepository.findById(produtoIds.get(i))
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

                BigDecimal qtd = (i < quantidades.size() && quantidades.get(i) != null)
                        ? quantidades.get(i)
                        : BigDecimal.ONE;

                BigDecimal precoUnit = obterPreco(produto, cliente.getPerfilPreco());
                BigDecimal valorTotal = precoUnit.multiply(qtd);
                totalPedido = totalPedido.add(valorTotal);

                PedidoItem item = PedidoItem.builder()
                        .pedido(pedido)
                        .produto(produto)
                        .quantidade(qtd)
                        .precoUnitario(precoUnit)
                        .valorTotal(valorTotal)
                        .build();
                pedido.getItens().add(item);
            }
        }

        pedido.setValorTotal(totalPedido);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoVenda confirmar(Long pedidoId) {
        PedidoVenda pedido = buscarPorId(pedidoId);
        if (pedido.getStatus() != StatusPedido.RASCUNHO) {
            throw new RuntimeException("Apenas pedidos em RASCUNHO podem ser confirmados.");
        }
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new RuntimeException("O pedido deve ter pelo menos um item.");
        }
        pedido.setStatus(StatusPedido.CONFIRMADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoVenda faturar(Long pedidoId) {
        PedidoVenda pedido = buscarPorId(pedidoId);
        if (pedido.getStatus() != StatusPedido.CONFIRMADO) {
            throw new RuntimeException("Apenas pedidos CONFIRMADOS podem ser faturados.");
        }

        // Baixar estoque dos produtos
        for (PedidoItem item : pedido.getItens()) {
            ProdutoAcabado produto = item.getProduto();
            int estoqueAtual = produto.getEstoqueAtual() != null ? produto.getEstoqueAtual() : 0;
            int qtdSaida = item.getQuantidade().intValue();
            if (estoqueAtual < qtdSaida) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNome()
                        + ". Disponível: " + estoqueAtual + ", solicitado: " + qtdSaida);
            }
            produto.setEstoqueAtual(estoqueAtual - qtdSaida);
            produtoRepository.save(produto);

            movimentacaoRepository.save(MovimentacaoEstoque.builder()
                    .tipo(TipoMovimentacao.SAIDA_PRODUTO_VENDA)
                    .produtoId(produto.getId())
                    .quantidade(item.getQuantidade())
                    .descricao("Venda — Pedido " + pedido.getNumeroPedido())
                    .build());
        }

        pedido.setStatus(StatusPedido.FATURADO);
        pedido.setDataFaturamento(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoVenda marcarEntregue(Long pedidoId) {
        PedidoVenda pedido = buscarPorId(pedidoId);
        if (pedido.getStatus() != StatusPedido.FATURADO) {
            throw new RuntimeException("Apenas pedidos FATURADOS podem ser marcados como entregues.");
        }

        LocalDateTime agora = LocalDateTime.now();
        pedido.setDataEntregaRealizada(agora);

        // Calcular OTIF (On Time In Full)
        // Comparando a dataPrevista (sem hora) com a dataRealizada (DataHora convertida
        // para Data local)
        if (pedido.getDataPrevisaoEntrega() != null) {
            LocalDate dataEntrega = agora.toLocalDate();
            if (!dataEntrega.isAfter(pedido.getDataPrevisaoEntrega())) {
                pedido.setOtifAvaliacao("NO PRAZO");
            } else {
                pedido.setOtifAvaliacao("ATRASADO");
            }
        } else {
            pedido.setOtifAvaliacao("N/A - SEM PREVISÃO");
        }

        pedido.setStatus(StatusPedido.ENTREGUE);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoVenda cancelar(Long pedidoId) {
        PedidoVenda pedido = buscarPorId(pedidoId);
        if (pedido.getStatus() == StatusPedido.FATURADO || pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw new RuntimeException("Pedido faturado/entregue não pode ser cancelado diretamente. Use estorno.");
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    private BigDecimal obterPreco(ProdutoAcabado produto, ClienteB2B.PerfilPreco perfil) {
        if (perfil == null)
            return produto.getPrecoVendaVarejo() != null ? produto.getPrecoVendaVarejo() : BigDecimal.ZERO;
        return switch (perfil) {
            case ATACADO -> produto.getPrecoVendaAtacado() != null ? produto.getPrecoVendaAtacado()
                    : produto.getPrecoVendaVarejo() != null ? produto.getPrecoVendaVarejo() : BigDecimal.ZERO;
            case DISTRIBUIDOR -> produto.getPrecoVendaDistribuidor() != null ? produto.getPrecoVendaDistribuidor()
                    : produto.getPrecoVendaVarejo() != null ? produto.getPrecoVendaVarejo() : BigDecimal.ZERO;
            default -> produto.getPrecoVendaVarejo() != null ? produto.getPrecoVendaVarejo() : BigDecimal.ZERO;
        };
    }

    private String gerarNumeroPedido() {
        String prefixo = "PV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        long count = pedidoRepository.count() + 1;
        return prefixo + String.format("%04d", count);
    }
}
