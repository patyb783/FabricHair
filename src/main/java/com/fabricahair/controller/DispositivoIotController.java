package com.fabricahair.controller;

import com.fabricahair.model.DispositivoIot;
import com.fabricahair.repository.DispositivoIotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/admin/iot")
public class DispositivoIotController {

    @Autowired
    private DispositivoIotRepository dispositivoIotRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("dispositivos", dispositivoIotRepository.findAll());
        return "admin/iot/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("dispositivo", new DispositivoIot());
        model.addAttribute("modoEdit", false);
        return "admin/iot/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        DispositivoIot d = dispositivoIotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado"));
        model.addAttribute("dispositivo", d);
        model.addAttribute("modoEdit", true);
        return "admin/iot/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute DispositivoIot dispositivo) {
        if (dispositivo.getId() == null) {
            dispositivo.setStatus(DispositivoIot.StatusSensor.OFFLINE);
        } else {
            DispositivoIot existente = dispositivoIotRepository.findById(dispositivo.getId()).orElse(null);
            if (existente != null) {
                dispositivo.setStatus(existente.getStatus());
                dispositivo.setUltimoPing(existente.getUltimoPing());
                dispositivo.setValorAtual(existente.getValorAtual());
            }
        }
        dispositivoIotRepository.save(dispositivo);
        return "redirect:/web/admin/iot";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        dispositivoIotRepository.deleteById(id);
        return "redirect:/web/admin/iot";
    }

    // Endpoint for testing the IoT sensors via Dashboard JS (Ping Simulator)
    @PostMapping("/simular-ping/{id}")
    @ResponseBody
    public String simularPing(@PathVariable Long id, @RequestParam String valor) {
        DispositivoIot d = dispositivoIotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Não encontrado"));

        // Simular a chamada que viria internamente do microcontrolador
        d.setValorAtual(valor);
        d.setUltimoPing(java.time.LocalDateTime.now());
        d.setStatus(DispositivoIot.StatusSensor.ONLINE);
        dispositivoIotRepository.save(d);

        return "OK";
    }
}
