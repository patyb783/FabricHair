package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_iot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogIot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private DispositivoIot dispositivo;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String valorLido; // "25.3" para temp, "100.0" para balança

    private String dadosAdicionais; // JSON de metadados se necessário
}
