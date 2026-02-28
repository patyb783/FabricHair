package com.fabricahair.service;

import com.fabricahair.model.*;
import com.fabricahair.model.NfeRegistro.StatusNfe;
import com.fabricahair.repository.NfeRegistroRepository;
import com.fabricahair.repository.PedidoVendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NfeService {

    @Value("${focusnfe.token}")
    private String token;

    @Value("${focusnfe.ambiente}")
    private String ambiente;

    @Value("${focusnfe.url.homologacao}")
    private String urlHomologacao;

    @Value("${focusnfe.url.producao}")
    private String urlProducao;

    @Value("${empresa.cnpj}")
    private String empresaCnpj;

    @Value("${empresa.razao_social}")
    private String empresaRazaoSocial;

    @Value("${empresa.nome_fantasia}")
    private String empresaNomeFantasia;

    @Value("${empresa.regime_tributario}")
    private String empresaRegime;

    @Autowired
    private NfeRegistroRepository nfeRepository;
    @Autowired
    private PedidoVendaRepository pedidoRepository;

    private String getBaseUrl() {
        return "homologacao".equals(ambiente) ? urlHomologacao : urlProducao;
    }

    private WebClient buildClient() {
        String credentials = token + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return WebClient.builder()
                .baseUrl(getBaseUrl())
                .defaultHeader("Authorization", "Basic " + encoded)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // =============================================
    // EMITIR NF-e A PARTIR DO PEDIDO DE VENDA
    // =============================================
    @Transactional
    public NfeRegistro emitirNfe(Long pedidoId) {
        PedidoVenda pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));

        // Verificar se já existe NF-e para este pedido
        Optional<NfeRegistro> existente = nfeRepository.findByPedidoId(pedidoId);
        if (existente.isPresent() && existente.get().getStatus() == StatusNfe.AUTORIZADA) {
            throw new RuntimeException(
                    "Este pedido já possui uma NF-e autorizada: " + existente.get().getChaveAcesso());
        }

        String referencia = gerarReferencia(pedido);

        // Criar/atualizar registro local
        NfeRegistro registro = existente.orElseGet(() -> NfeRegistro.builder()
                .pedido(pedido)
                .referenciaFocus(referencia)
                .build());
        registro.setStatus(StatusNfe.PROCESSANDO);
        registro.setReferenciaFocus(referencia);
        registro = nfeRepository.save(registro);

        // Montar payload e enviar para o Focus NFe
        try {
            Map<String, Object> payload = montarPayloadNfe(pedido, referencia);
            WebClient client = buildClient();

            Map resposta = client.post()
                    .uri("/nfe?ref=" + referencia)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            atualizarRegistroComResposta(registro, resposta);

        } catch (WebClientResponseException ex) {
            registro.setStatus(StatusNfe.ERRO);
            registro.setMensagemSefaz("Erro HTTP " + ex.getStatusCode() + ": " + ex.getResponseBodyAsString());
            registro.setAtualizadoEm(LocalDateTime.now());
        } catch (Exception ex) {
            registro.setStatus(StatusNfe.ERRO);
            registro.setMensagemSefaz("Erro ao emitir: " + ex.getMessage());
            registro.setAtualizadoEm(LocalDateTime.now());
        }

        return nfeRepository.save(registro);
    }

    // =============================================
    // CONSULTAR STATUS NO FOCUS NFe
    // =============================================
    @Transactional
    public NfeRegistro consultarStatus(Long registroId) {
        NfeRegistro registro = nfeRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro NF-e não encontrado."));

        try {
            Map resposta = buildClient().get()
                    .uri("/nfe/" + registro.getReferenciaFocus())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            atualizarRegistroComResposta(registro, resposta);
        } catch (Exception ex) {
            registro.setMensagemSefaz("Erro ao consultar: " + ex.getMessage());
            registro.setAtualizadoEm(LocalDateTime.now());
        }

        return nfeRepository.save(registro);
    }

    // =============================================
    // CANCELAR NF-e
    // =============================================
    @Transactional
    public NfeRegistro cancelarNfe(Long registroId, String justificativa) {
        NfeRegistro registro = nfeRepository.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro NF-e não encontrado."));

        if (registro.getStatus() != StatusNfe.AUTORIZADA) {
            throw new RuntimeException("Somente NF-e autorizadas podem ser canceladas.");
        }
        if (justificativa == null || justificativa.trim().length() < 15) {
            throw new RuntimeException("A justificativa deve ter ao menos 15 caracteres.");
        }

        try {
            Map<String, String> body = Map.of("justificativa", justificativa.trim());
            Map resposta = buildClient().delete()
                    .uri("/nfe/" + registro.getReferenciaFocus())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String status = resposta != null ? String.valueOf(resposta.get("status_sefaz")) : "";
            if ("135".equals(status)) {
                registro.setStatus(StatusNfe.CANCELADA);
                registro.setCanceladoEm(LocalDateTime.now());
                registro.setMotivoCancelamento(justificativa);
                // Atualizar pedido de venda
                PedidoVenda pedido = registro.getPedido();
                pedido.setNfeChaveAcesso(null);
                pedidoRepository.save(pedido);
            } else {
                registro.setMensagemSefaz("Cancelamento não aceito SEFAZ: " + resposta);
            }
            registro.setAtualizadoEm(LocalDateTime.now());
        } catch (Exception ex) {
            registro.setMensagemSefaz("Erro ao cancelar: " + ex.getMessage());
            registro.setAtualizadoEm(LocalDateTime.now());
        }

        return nfeRepository.save(registro);
    }

    public List<NfeRegistro> listarTodas() {
        return nfeRepository.findAllByOrderByCriadoEmDesc();
    }

    public Optional<NfeRegistro> buscarPorPedido(Long pedidoId) {
        return nfeRepository.findByPedidoId(pedidoId);
    }

    // =============================================
    // MONTAR PAYLOAD NF-e 4.0 PARA FOCUS NFe
    // =============================================
    private Map<String, Object> montarPayloadNfe(PedidoVenda pedido, String referencia) {
        Map<String, Object> nfe = new LinkedHashMap<>();

        boolean isHomologacao = "homologacao".equals(ambiente);

        // ---- EMITENTE ----
        nfe.put("cnpj_emitente", empresaCnpj);
        nfe.put("nome_emitente",
                isHomologacao ? "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL" : empresaRazaoSocial);
        nfe.put("nome_fantasia_emitente", empresaNomeFantasia);
        nfe.put("regime_tributario_emitente", empresaRegime);

        // ---- DESTINATÁRIO ----
        ClienteB2B cli = pedido.getCliente();
        nfe.put("cnpj_destinatario", cli.getCnpj());
        nfe.put("nome_destinatario", cli.getRazaoSocial());
        nfe.put("email_destinatario", cli.getEmail() != null ? cli.getEmail() : "");
        nfe.put("indicador_inscricao_estadual_destinatario", cli.getInscricaoEstadual() != null ? 1 : 9);
        if (cli.getInscricaoEstadual() != null) {
            nfe.put("inscricao_estadual_destinatario", cli.getInscricaoEstadual());
        }

        // Endereço destinatário
        nfe.put("logradouro_destinatario", cli.getEndereco() != null ? cli.getEndereco() : "A DEFINIR");
        nfe.put("numero_destinatario", cli.getNumero() != null ? cli.getNumero() : "S/N");
        nfe.put("complemento_destinatario", cli.getComplemento() != null ? cli.getComplemento() : "");
        nfe.put("bairro_destinatario", cli.getBairro() != null ? cli.getBairro() : "A DEFINIR");
        nfe.put("municipio_destinatario", cli.getCidade() != null ? cli.getCidade() : "A DEFINIR");
        nfe.put("uf_destinatario", cli.getEstado() != null ? cli.getEstado() : "SP");
        nfe.put("cep_destinatario", cli.getCep() != null ? cli.getCep() : "00000000");
        nfe.put("pais_destinatario", "Brasil");

        // ---- DADOS GERAIS ----
        nfe.put("natureza_operacao", "VENDA DE PRODUTO INDUSTRIALIZADO");
        nfe.put("forma_pagamento", 0);
        nfe.put("tipo_documento", 1); // 1 = Saída
        nfe.put("local_destino", 1); // 1 = Operação interna
        nfe.put("modalidade_frete", 9); // 9 = Sem frete
        nfe.put("forma_emissao", 1); // 1 = Emissão normal
        nfe.put("finalidade_emissao", 1); // 1 = NF-e normal
        nfe.put("consumidor_final", 0); // 0 = Não (B2B)
        nfe.put("presenca_comprador", 0); // 0 = Não se aplica

        // ---- ITENS ----
        List<Map<String, Object>> itens = new ArrayList<>();
        int numeroItem = 1;
        for (PedidoItem item : pedido.getItens()) {
            Map<String, Object> it = new LinkedHashMap<>();
            ProdutoAcabado prod = item.getProduto();

            it.put("numero_item", numeroItem++);
            it.put("codigo_produto", prod.getSku() != null ? prod.getSku() : String.valueOf(prod.getId()));
            it.put("codigo_barras_comercial", prod.getEan() != null ? prod.getEan() : "SEM GTIN");
            it.put("descricao", isHomologacao ? "PRODUTO HOMOLOGACAO" : prod.getNome());
            it.put("codigo_ncm", prod.getNcm() != null ? prod.getNcm().replace(".", "") : "33059000");
            it.put("cfop", "5101"); // Venda de produção própria dentro do estado
            it.put("unidade_comercial", prod.getUnidade() != null ? prod.getUnidade() : "UN");
            it.put("unidade_tributavel", prod.getUnidade() != null ? prod.getUnidade() : "UN");

            BigDecimal qtd = item.getQuantidade() != null ? item.getQuantidade() : BigDecimal.ONE;
            BigDecimal preco = item.getPrecoUnitario() != null ? item.getPrecoUnitario() : BigDecimal.ZERO;
            BigDecimal total = item.getValorTotal() != null ? item.getValorTotal() : preco.multiply(qtd);

            it.put("quantidade_comercial", qtd.setScale(4, RoundingMode.HALF_UP).toPlainString());
            it.put("quantidade_tributavel", qtd.setScale(4, RoundingMode.HALF_UP).toPlainString());
            it.put("valor_unitario_comercial", preco.setScale(10, RoundingMode.HALF_UP).toPlainString());
            it.put("valor_unitario_tributavel", preco.setScale(10, RoundingMode.HALF_UP).toPlainString());
            it.put("valor_bruto", total.setScale(2, RoundingMode.HALF_UP).toPlainString());

            // Impostos — Simples Nacional: CSOSN 400 (tributado sem permissão de crédito)
            it.put("icms_situacao_tributaria", "400");
            it.put("icms_origem", 0);
            it.put("pis_situacao_tributaria", "07"); // 07 = Operação isenta
            it.put("cofins_situacao_tributaria", "07");

            itens.add(it);
        }
        nfe.put("items", itens);

        // ---- TOTAIS ----
        BigDecimal totalNfe = pedido.getValorTotal() != null ? pedido.getValorTotal() : BigDecimal.ZERO;
        nfe.put("valor_produtos", totalNfe.setScale(2, RoundingMode.HALF_UP).toPlainString());
        nfe.put("valor_total", totalNfe.setScale(2, RoundingMode.HALF_UP).toPlainString());

        // ---- PAGAMENTO ----
        List<Map<String, Object>> pagamentos = new ArrayList<>();
        Map<String, Object> pag = new LinkedHashMap<>();
        pag.put("forma_pagamento", "01"); // 01 = Dinheiro (padrão para emissão)
        pag.put("valor_pagamento", totalNfe.setScale(2, RoundingMode.HALF_UP).toPlainString());
        pagamentos.add(pag);
        nfe.put("formas_pagamento", pagamentos);

        return nfe;
    }

    @SuppressWarnings("unchecked")
    private void atualizarRegistroComResposta(NfeRegistro registro, Map<?, ?> respostaRaw) {
        if (respostaRaw == null)
            return;
        Map<String, Object> resposta = (Map<String, Object>) respostaRaw;

        String statusFocus = String.valueOf(resposta.getOrDefault("status", ""));
        String statusSefaz = String.valueOf(resposta.getOrDefault("status_sefaz", ""));
        String chave = String.valueOf(resposta.getOrDefault("chave_nfe", ""));
        String numero = String.valueOf(resposta.getOrDefault("numero", ""));
        String serie = String.valueOf(resposta.getOrDefault("serie", ""));
        String mensagem = String.valueOf(resposta.getOrDefault("mensagem_sefaz", ""));
        String urlDanfe = String.valueOf(resposta.getOrDefault("caminho_danfe", ""));
        String urlXml = String.valueOf(resposta.getOrDefault("caminho_xml_nota_fiscal", ""));

        registro.setStatusSefaz(statusSefaz);
        registro.setMensagemSefaz(mensagem);
        registro.setAtualizadoEm(LocalDateTime.now());

        if (!numero.isEmpty() && !"null".equals(numero))
            registro.setNumeroNfe(numero);
        if (!serie.isEmpty() && !"null".equals(serie))
            registro.setSerie(serie);
        if (!urlDanfe.isEmpty() && !"null".equals(urlDanfe))
            registro.setUrlDanfe(urlDanfe);
        if (!urlXml.isEmpty() && !"null".equals(urlXml))
            registro.setUrlXml(urlXml);

        // Status SEFAZ 100 = Autorizada, 135 = Cancelada
        if ("100".equals(statusSefaz) || "autorizado".equalsIgnoreCase(statusFocus)) {
            registro.setStatus(StatusNfe.AUTORIZADA);
            if (!chave.isEmpty() && !"null".equals(chave)) {
                registro.setChaveAcesso(chave);
                // Atualizar pedido com a chave
                PedidoVenda pedido = registro.getPedido();
                pedido.setNfeChaveAcesso(chave);
            }
        } else if ("135".equals(statusSefaz)) {
            registro.setStatus(StatusNfe.CANCELADA);
        } else if ("processando_autorizacao".equals(statusFocus) || "recebido".equals(statusFocus)) {
            registro.setStatus(StatusNfe.PROCESSANDO);
        } else if ("erro_autorizacao".equals(statusFocus) || "denegado".equals(statusFocus)) {
            registro.setStatus("denegado".equals(statusFocus) ? StatusNfe.DENEGADA : StatusNfe.ERRO);
        }
    }

    private String gerarReferencia(PedidoVenda pedido) {
        return "FH-PV" + pedido.getId() + "-" + System.currentTimeMillis();
    }
}
