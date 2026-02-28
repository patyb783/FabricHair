package com.fabricahair.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {

    @Autowired private JwtAuthenticationFilter jwtFilter;
    @Autowired private JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/api/auth/**", "/error", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/web/admin/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/web/producao/**").hasAnyRole("ADMIN", "GERENTE", "PRODUCAO")
                .requestMatchers("/web/estoque/**").hasAnyRole("ADMIN", "GERENTE", "ESTOQUE", "PRODUCAO")
                .requestMatchers("/web/pedidos/**", "/web/clientes/**").hasAnyRole("ADMIN", "GERENTE", "VENDAS")
                .requestMatchers("/web/fiscal/**").hasAnyRole("ADMIN", "GERENTE", "FINANCEIRO")
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
}
