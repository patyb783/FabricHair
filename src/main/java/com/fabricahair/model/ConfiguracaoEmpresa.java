package com.fabricahair.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracao_empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoEmpresa {

    @Id
    private Long id; // Will always be 1

    @Column(name = "razao_social")
    private String razaoSocial;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    private String cnpj;
    private String telefone;
    private String email;
    private String endereco;

    @Column(name = "logo_base64", columnDefinition = "TEXT")
    private String logoBase64;
}
