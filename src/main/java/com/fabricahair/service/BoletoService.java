package com.fabricahair.service;

import com.fabricahair.model.TituloFinanceiro;
import com.fabricahair.repository.TituloFinanceiroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BoletoService {

    @Autowired
    private TituloFinanceiroRepository tituloRepository;

    @Transactional
    public TituloFinanceiro gerarBoleto(Long tituloId) {
        TituloFinanceiro titulo = tituloRepository.findById(tituloId)
                .orElseThrow(() -> new RuntimeException("Título não encontrado para emissão de boleto."));

        if (titulo.getTipo() != TituloFinanceiro.TipoTitulo.RECEBER) {
            throw new RuntimeException("Apenas títulos do tipo RECEBER podem gerar boleto bancário.");
        }

        if (titulo.getStatus() == TituloFinanceiro.StatusTitulo.PAGO) {
            throw new RuntimeException("Este título já consta como pago.");
        }

        if (titulo.getLinhaDigitavel() != null) {
            throw new RuntimeException("Este título já possui um boleto gerado.");
        }

        // ==========================================
        // Integração Homologação Bancária (Mock)
        // Aqui conectaríamos com API Bradesco, Asaas, Itaú, etc..
        // Mockando retorno de sucesso do Gateway Bancário para testes VIP.
        // ==========================================

        // Gerar Nosso Número fictício
        String fNossoNumero = "109" + String.format("%08d", titulo.getId());

        // Gerar Linha Digitável fictícia
        String mValor = titulo.getValorOriginal().toString().replace(".", "");
        String fLinhaDigitavel = "34191.09008 " + fNossoNumero + ".123456 00000.000000 1 900000000" + mValor;

        // Gerar URL de PDF Fictício
        String hash = UUID.randomUUID().toString().substring(0, 8);
        String fLink = "https://sandbox.gatewaybancario.com.br/boletos/imprimir/" + hash + "-" + titulo.getId();

        titulo.setNossoNumero(fNossoNumero);
        titulo.setLinhaDigitavel(fLinhaDigitavel);
        titulo.setLinkBoleto(fLink);

        return tituloRepository.save(titulo);
    }
}
