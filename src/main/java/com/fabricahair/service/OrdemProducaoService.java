package com.fabricahair.service;

import com.fabricahair.model.*;
import com.fabricahair.model.OrdemProducao.StatusOP;
import com.fabricahair.model.Lote.StatusLote;
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
public class OrdemProducaoService {

    @Autowired
    private OrdemProducaoRepository opRepository;
    @Autowired
    private InsumoRepository insumoRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private ProdutoAcabadoRepository produtoRepository;
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    public List<OrdemProducao> listarTodas() {
        return opRepository.findAll();
    }

    public List<OrdemProducao> listarAtivas() {
        return opRepository.findByStatusIn(List.of(StatusOP.PLANEJADA, StatusOP.EM_PRODUCAO));
    }

    public OrdemProducao buscarPorId(Long id) {
        return opRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de Produção não encontrada: " + id));
    }

    @Transactional
    public OrdemProducao criar(Long produtoId, BigDecimal quantidade, List<Long> insumoIds,
            List<BigDecimal> quantidadesInsumos, String observacoes) {
        ProdutoAcabado produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        String numeroOp = gerarNumeroOP();

        OrdemProducao op = OrdemProducao.builder()
                .numeroOp(numeroOp)
                .produto(produto)
                .quantidadePlanejada(quantidade)
                .status(StatusOP.PLANEJADA)
                .observacoes(observacoes)
                .insumos(new ArrayList<>())
                .build();

        op = opRepository.save(op);

        // Adicionar insumos da OP
        if (insumoIds != null) {
            for (int i = 0; i < insumoIds.size(); i++) {
                if (insumoIds.get(i) == null)
                    continue;
                Insumo insumo = insumoRepository.findById(insumoIds.get(i))
                        .orElseThrow(() -> new RuntimeException("Insumo não encontrado."));
                OpInsumo opInsumo = OpInsumo.builder()
                        .ordemProducao(op)
                        .insumo(insumo)
                        .quantidadeNecessaria(
                                i < quantidadesInsumos.size() ? quantidadesInsumos.get(i) : BigDecimal.ZERO)
                        .build();
                op.getInsumos().add(opInsumo);
            }
            op = opRepository.save(op);
        }

        return op;
    }

    @Transactional
    public OrdemProducao iniciarProducao(Long opId) {
        OrdemProducao op = buscarPorId(opId);

        if (op.getStatus() != StatusOP.PLANEJADA) {
            throw new RuntimeException("Apenas OPs 'PLANEJADA' podem ser iniciadas.");
        }

        // Verificar e baixar estoque de insumos
        List<String> erros = new ArrayList<>();
        if (op.getInsumos() != null) {
            for (OpInsumo opInsumo : op.getInsumos()) {
                Insumo insumo = opInsumo.getInsumo();
                BigDecimal necessario = opInsumo.getQuantidadeNecessaria();
                if (insumo.getEstoqueAtual() == null || insumo.getEstoqueAtual().compareTo(necessario) < 0) {
                    erros.add(String.format("%s: necessário %.3f, disponível %.3f %s",
                            insumo.getNome(), necessario,
                            insumo.getEstoqueAtual() != null ? insumo.getEstoqueAtual() : BigDecimal.ZERO,
                            insumo.getUnidade() != null ? insumo.getUnidade() : ""));
                }
            }
        }

        if (!erros.isEmpty()) {
            throw new RuntimeException("Estoque insuficiente para iniciar a OP:\n" + String.join("\n", erros));
        }

        // Baixar insumos do estoque
        if (op.getInsumos() != null) {
            for (OpInsumo opInsumo : op.getInsumos()) {
                Insumo insumo = opInsumo.getInsumo();
                BigDecimal novoEstoque = insumo.getEstoqueAtual().subtract(opInsumo.getQuantidadeNecessaria());
                insumo.setEstoqueAtual(novoEstoque);
                opInsumo.setQuantidadeUsada(opInsumo.getQuantidadeNecessaria());
                insumoRepository.save(insumo);

                // Registrar movimentação
                movimentacaoRepository.save(MovimentacaoEstoque.builder()
                        .tipo(TipoMovimentacao.SAIDA_INSUMO_OP)
                        .insumoId(insumo.getId())
                        .ordemProducaoId(opId)
                        .quantidade(opInsumo.getQuantidadeNecessaria())
                        .descricao("Consumo na OP " + op.getNumeroOp())
                        .build());
            }
        }

        op.setStatus(StatusOP.EM_PRODUCAO);
        op.setDataInicio(LocalDateTime.now());
        return opRepository.save(op);
    }

    @Transactional
    public OrdemProducao finalizarProducao(Long opId, BigDecimal quantidadeProduzida,
            LocalDate validade, String observacoesLote,
            BigDecimal quantidadeRefugo, String motivoRefugo,
            BigDecimal custoMaoDeObra, BigDecimal custoEnergia) {
        OrdemProducao op = buscarPorId(opId);

        if (op.getStatus() != StatusOP.EM_PRODUCAO) {
            throw new RuntimeException("Apenas OPs 'EM_PRODUCAO' podem ser finalizadas.");
        }

        // Calculate Cost
        BigDecimal custoInsumos = BigDecimal.ZERO;
        if (op.getInsumos() != null) {
            for (OpInsumo opInsumo : op.getInsumos()) {
                BigDecimal qtd = opInsumo.getQuantidadeUsada() != null ? opInsumo.getQuantidadeUsada()
                        : opInsumo.getQuantidadeNecessaria();
                BigDecimal preco = opInsumo.getInsumo().getPrecoUnitario() != null
                        ? opInsumo.getInsumo().getPrecoUnitario()
                        : BigDecimal.ZERO;
                custoInsumos = custoInsumos.add(qtd.multiply(preco));
            }
        }

        BigDecimal vMaoDeObra = custoMaoDeObra != null ? custoMaoDeObra : BigDecimal.ZERO;
        BigDecimal vEnergia = custoEnergia != null ? custoEnergia : BigDecimal.ZERO;
        BigDecimal custoTotal = custoInsumos.add(vMaoDeObra).add(vEnergia);
        BigDecimal custoUnitario = quantidadeProduzida.compareTo(BigDecimal.ZERO) > 0
                ? custoTotal.divide(quantidadeProduzida, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Gerar lote em quarentena
        String numeroLote = gerarNumeroLote(op.getProduto());
        Lote lote = Lote.builder()
                .produto(op.getProduto())
                .numeroLote(numeroLote)
                .dataFabricacao(LocalDate.now())
                .dataValidade(validade)
                .quantidade(quantidadeProduzida)
                .quantidadeDisponivel(quantidadeProduzida)
                .status(StatusLote.EM_QUARENTENA)
                .ordemProducao(op)
                .build();
        lote = loteRepository.save(lote);

        // Finalizar OP
        op.setStatus(StatusOP.FINALIZADA);
        op.setDataFim(LocalDateTime.now());
        op.setQuantidadeProduzida(quantidadeProduzida);
        op.setQuantidadeRefugo(quantidadeRefugo != null ? quantidadeRefugo : BigDecimal.ZERO);
        op.setMotivoRefugo(motivoRefugo);
        op.setCustoInsumos(custoInsumos);
        op.setCustoMaoDeObra(vMaoDeObra);
        op.setCustoEnergia(vEnergia);
        op.setCustoTotal(custoTotal);
        op.setCustoUnitario(custoUnitario);

        if (observacoesLote != null && !observacoesLote.isBlank()) {
            op.setObservacoes((op.getObservacoes() != null ? op.getObservacoes() + "\n" : "") + observacoesLote);
        }

        return opRepository.save(op);
    }

    @Transactional
    public void cancelar(Long opId) {
        OrdemProducao op = buscarPorId(opId);
        if (op.getStatus() == StatusOP.FINALIZADA) {
            throw new RuntimeException("Não é possível cancelar uma OP já finalizada.");
        }
        op.setStatus(StatusOP.CANCELADA);
        opRepository.save(op);
    }

    private String gerarNumeroOP() {
        String prefixo = "OP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        long count = opRepository.count() + 1;
        return prefixo + String.format("%04d", count);
    }

    private String gerarNumeroLote(ProdutoAcabado produto) {
        String sku = produto.getSku() != null ? produto.getSku() : "PROD";
        return "LOT-" + sku + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%03d", loteRepository.count() + 1);
    }
}
