package com.fabricahair.controller;

import com.fabricahair.service.IotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot")
public class IotRestController {

    @Autowired
    private IotService iotService;

    // As máquinas industriais enviarão requisições HTTP POST para estes endpoints
    // Nenhuma sessão web (Cookies/JSESSIONID) é necessária, operam de forma isolada
    // autenticados via MAC Address (Hardware Signature).

    @PostMapping("/push")
    public ResponseEntity<String> receberLeituraGenerica(@RequestBody IotPayload payload) {
        try {
            iotService.processarLeituraSensor(payload.getMacAddress(), payload.getValor(), payload.getIpLocal());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("[IoT ERROR] " + e.getMessage());
            return ResponseEntity.badRequest().body("FALHA: " + e.getMessage());
        }
    }

    @PostMapping("/temperatura")
    public ResponseEntity<String> receberTemperatura(@RequestBody IotPayload payload) {
        return receberLeituraGenerica(payload);
    }

    @PostMapping("/balanca")
    public ResponseEntity<String> receberBalanca(@RequestBody IotPayload payload) {
        return receberLeituraGenerica(payload);
    }

    @PostMapping("/esteira/contagem")
    public ResponseEntity<String> receberContagemEsteira(@RequestBody IotPayload payload) {
        return receberLeituraGenerica(payload);
    }

    @PostMapping("/rfid")
    public ResponseEntity<String> receberArrayRfid(@RequestBody IotPayload payload) {
        // payload.valor virá como string JSON (Ex: "['7891010', '7891011']")
        return receberLeituraGenerica(payload);
    }

    public static class IotPayload {
        private String macAddress;
        private String valor;
        private String ipLocal;

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }

        public String getIpLocal() {
            return ipLocal;
        }

        public void setIpLocal(String ipLocal) {
            this.ipLocal = ipLocal;
        }
    }
}
