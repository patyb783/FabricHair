package com.fabricahair.service;

import com.fabricahair.model.Lote;
import com.fabricahair.model.MovimentacaoEstoque;
import com.fabricahair.model.MovimentacaoEstoque.TipoMovimentacao;
import com.fabricahair.model.ProdutoAcabado;
import com.fabricahair.repository.LoteRepository;
import com.fabricahair.repository.MovimentacaoEstoqueRepository;
import com.fabricahair.repository.ProdutoAcabadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QualidadeService {

    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private ProdutoAcabadoRepository produtoRepository;
    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    public List<Lote> listarEmQuarentena() {
        return loteRepository.findByStatus(Lote.StatusLote.EM_QUARENTENA);
    }

    @Transactional
    public void aprovarLote(Long loteId, String analista, String localizacao) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new RuntimeException("Lote não encontrado: " + loteId));

        if (lote.getStatus() != Lote.StatusLote.EM_QUARENTENA) {
            throw new RuntimeException("O Lote não está em Quarentena.");
        }

        lote.setStatus(Lote.StatusLote.APROVADO);
        lote.setAnalistaQualidade(analista);
        lote.setDataLiberacao(LocalDateTime.now());
        if (localizacao != null && !localizacao.trim().isEmpty()) {
            lote.setLocalizacao(localizacao.trim());
        }
        loteRepository.save(lote);

        // Somar ao estoque físico do produto
        ProdutoAcabado produto = lote.getProduto();
        int estoqueAtual = produto.getEstoqueAtual() != null ? produto.getEstoqueAtual() : 0;
        produto.setEstoqueAtual(estoqueAtual + lote.getQuantidadeDisponivel().intValue());
        produtoRepository.save(produto);

        // Registrar entrada física disponível
        movimentacaoRepository.save(MovimentacaoEstoque.builder()
                .tipo(TipoMovimentacao.ENTRADA_PRODUTO_OP)
                .produtoId(produto.getId())
                .loteId(lote.getId())
                .ordemProducaoId(lote.getOrdemProducao() != null ? lote.getOrdemProducao().getId() : null)
                .quantidade(lote.getQuantidadeDisponivel())
                .descricao("Lote Aprovado na Qualidade (" + lote.getNumeroLote() + ")")
                .build());
    }

    @Transactional
    public void reprovarLote(Long loteId, String analista, String justificativa) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new RuntimeException("Lote não encontrado: " + loteId));

        if (lote.getStatus() != Lote.StatusLote.EM_QUARENTENA) {
            throw new RuntimeException("O Lote não está em Quarentena.");
        }

        lote.setStatus(Lote.StatusLote.REPROVADO);
        lote.setAnalistaQualidade(analista);
        lote.setDataLiberacao(LocalDateTime.now());
        // For reprovado, we don't add to stock.
        // Option: write an observer property to "observacoesLote" in OP? Or just rely
        // on MotivoRefugo inside OP.
        // Since lote doesn't have an observation field besides OP, maybe append it.
        if (lote.getOrdemProducao() != null) {
            String obs = lote.getOrdemProducao().getObservacoes() == null ? ""
                    : lote.getOrdemProducao().getObservacoes() + "\n";
            lote.getOrdemProducao()
                    .setObservacoes(obs + "QUALIDADE REPROVOU O LOTE " + lote.getNumeroLote() + ": " + justificativa);
        }

        loteRepository.save(lote);
    }
}
