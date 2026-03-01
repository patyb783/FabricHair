package com.fabricahair.controller;

import com.fabricahair.model.DispositivoIot;
import com.fabricahair.repository.DispositivoIotRepository;
import com.fabricahair.repository.LogIotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/iot")
public class IotDashboardController {

    @Autowired
    private DispositivoIotRepository dispositivoRepository;

    @Autowired
    private LogIotRepository logRepository;

    @GetMapping("/temperatura")
    public String painelTemperatura(Model model) {
        List<DispositivoIot> termometros = dispositivoRepository
                .findByTipoSensor(DispositivoIot.TipoSensor.TEMPERATURA);

        // Em um cenário real, carregaríamos o histórico das últimas 24h para plotar um
        // gráfico (Chart.js)
        model.addAttribute("sensores", termometros);

        return "iot/temperatura";
    }

    @GetMapping("/balanca")
    public String painelBalanca(Model model) {
        List<DispositivoIot> balancas = dispositivoRepository.findByTipoSensor(DispositivoIot.TipoSensor.BALANCA);
        model.addAttribute("sensores", balancas);
        return "iot/balanca";
    }

    @GetMapping("/esteiras")
    public String painelEsteira(Model model) {
        List<DispositivoIot> esteiras = dispositivoRepository.findByTipoSensor(DispositivoIot.TipoSensor.ESTEIRA);
        model.addAttribute("sensores", esteiras);
        return "iot/esteira";
    }

    @GetMapping("/leitura-atual/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public String obterLeituraAtual(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return dispositivoRepository.findById(id)
                .map(DispositivoIot::getValorAtual)
                .orElse("0");
    }
}
