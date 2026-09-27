package com.coworking.api.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. DESHABILITAR CSRF (Obligatorio para recibir POST sin token CSRF)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Sesiones sin estado (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso público a registro y login
                        .requestMatchers("/auth/**").permitAll()
                        // Permitir /error para que Spring pueda devolver respuestas JSON de excepciones
                        .requestMatchers("/error").permitAll()
                        // Permitir Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Permitir Actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                // 4. Añadir el filtro JWT antes del filtro de autenticación por defecto
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}