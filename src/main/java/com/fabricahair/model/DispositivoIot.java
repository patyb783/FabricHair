package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispositivos_iot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoIot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome; // Ex: Balança Doca 1, Termômetro Área Fria

    @Column(unique = true, nullable = false)
    private String macAddress; // Autenticador (MAC ou Token UUID do dispositivo físico)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSensor tipoSensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSensor status;

    private String valorAtual; // Armazena a última leitura bruta, ex: "24.5", "150.0kg", "[EAN1, EAN2]"

    private LocalDateTime ultimoPing; // Usado para detectar se o sensor caiu da rede (offline)

    private String ipLocal; // IP de rede interna para eventuais comandos bidirecionais

    public enum TipoSensor {
        BALANCA,
        ESTEIRA,
        TEMPERATURA,
        UMIDADE,
        RFID
    }

    public enum StatusSensor {
        ONLINE,
        OFFLINE,
        MANUTENCAO
    }
}
