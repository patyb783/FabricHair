package com.fabricahair.service;

import com.fabricahair.model.DispositivoIot;
import com.fabricahair.model.LogIot;
import com.fabricahair.repository.DispositivoIotRepository;
import com.fabricahair.repository.LogIotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IotService {

    @Autowired
    private DispositivoIotRepository dispositivoRepository;

    @Autowired
    private LogIotRepository logRepository;

    public List<DispositivoIot> listarDispositivos() {
        return dispositivoRepository.findAll();
    }

    public DispositivoIot buscarDispositivoPorId(Long id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));
    }

    @Transactional
    public DispositivoIot registrarDispositivo(DispositivoIot dispositivo) {
        dispositivo.setStatus(DispositivoIot.StatusSensor.OFFLINE);
        return dispositivoRepository.save(dispositivo);
    }

    /**
     * Ponto de entrada das Requisições das Máquinas (C++ / Arduino / ESP / CLPs)
     */
    @Transactional
    public void processarLeituraSensor(String macAddress, String valor, String ipLocal) {
        DispositivoIot disp = dispositivoRepository.findByMacAddress(macAddress)
                .orElseThrow(
                        () -> new RuntimeException("Dispositivo (MAC: " + macAddress + ") não autorizado na Rede."));

        LocalDateTime agora = LocalDateTime.now();

        // Atualiza os painéis do Dispositivo
        disp.setValorAtual(valor);
        disp.setUltimoPing(agora);
        disp.setStatus(DispositivoIot.StatusSensor.ONLINE);
        if (ipLocal != null && !ipLocal.isEmpty()) {
            disp.setIpLocal(ipLocal);
        }
        dispositivoRepository.save(disp);

        // Registra o Histórico para Gráficos
        LogIot log = LogIot.builder()
                .dispositivo(disp)
                .dataHora(agora)
                .valorLido(valor)
                .build();

        logRepository.save(log);

        // Ações engatilhadas por tipo de sensor
        tratarRegraDeNegocioDoSensor(disp, valor);
    }

    private void tratarRegraDeNegocioDoSensor(DispositivoIot disp, String valor) {
        switch (disp.getTipoSensor()) {
            case TEMPERATURA:
                // TODO: Verificar se excedeu a margem configurável de segurança (Ex: > 30ºC) e
                // disparar notificação.
                break;
            case BALANCA:
                // TODO: Fazer push web-socket (ou polling) para a tela da OP / Quarentena
                // exibir o peso em tempo real.
                break;
            case ESTEIRA:
                // TODO: Incrementar "quantidadeProduzida" de uma OP ativa vinculada à linha.
                break;
            case RFID:
                System.out.println(
                        ">>> [PORTAL RFID] Detectou aproximação da empilhadeira! MAC: " + disp.getMacAddress());
                System.out.println(">>> [PORTAL RFID] Lendo múltiplas tags instantaneamente: " + valor);
                System.out.println(">>> [WMS] Auto-faturamento via Radiofrequência engatilhado para os EPCs lidos.");
                break;
            default:
                break;
        }
    }
}
