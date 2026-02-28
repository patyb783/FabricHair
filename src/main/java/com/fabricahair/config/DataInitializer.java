package com.fabricahair.config;

import com.fabricahair.model.Usuario;
import com.fabricahair.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepo.findByUsername("admin").isEmpty()) {
            Usuario admin = Usuario.builder()
                .username("admin")
                .password(passwordEncoder.encode("FabricHair@2026"))
                .email("admin@fabricahair.com.br")
                .role(Usuario.Role.ADMIN)
                .ativo(true)
                .build();
            usuarioRepo.save(admin);
            System.out.println(">>> Usuário admin criado: admin / FabricHair@2026");
        }
    }
}