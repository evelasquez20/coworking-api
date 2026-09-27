package com.coworking.api.service.impl;

import com.coworking.api.config.security.JwtUtils;
import com.coworking.api.domain.dto.AuthResponse;
import com.coworking.api.domain.dto.LoginRequest;
import com.coworking.api.domain.dto.RegisterRequest;
import com.coworking.api.domain.entity.User;
import com.coworking.api.domain.enums.RoleEnum;
import com.coworking.api.exception.BusinessException;
import com.coworking.api.exception.ErrorCode;
import com.coworking.api.repository.UserRepository;
import com.coworking.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Iniciando proceso de registro para el usuario con email: {}", request.email());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Registro fallido: El correo electrónico {} ya está en uso.", request.email());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        RoleEnum role = request.role() != null ? request.role() : RoleEnum.ROLE_USER;
        log.debug("Rol asignado para {}: {}", request.email(), role);

        try {
            User user = User.builder()
                    .name(request.name())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .role(role)
                    .build();

            userRepository.save(user);
            log.info("Usuario guardado exitosamente en base de datos con ID: {}", user.getId());

            String token = jwtUtils.generateToken(user);
            log.info("Token JWT generado exitosamente para el usuario recién registrado: {}", request.email());

            return new AuthResponse(token, user.getEmail(), user.getRole().name());
        } catch (Exception e) {
            log.error("Error inesperado durante el registro del usuario {}: {}", request.email(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Iniciando intento de login para el email: {}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            log.debug("Autenticación de Spring Security superada para: {}", request.email());
        } catch (AuthenticationException e) {
            log.warn("Fallo de autenticación para el email {}. Credenciales inválidas o cuenta deshabilitada. Motivo: {}",
                    request.email(), e.getMessage());
            throw e;
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Login fallido crítico: Usuario pasó autenticación pero no se encontró en BD con email {}", request.email());
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        try {
            String token = jwtUtils.generateToken(user);
            log.info("Login exitoso. Token generado correctamente para el usuario: {}", user.getEmail());
            return new AuthResponse(token, user.getEmail(), user.getRole().name());
        } catch (Exception e) {
            log.error("Error crítico al firmar/generar el token JWT para {}: {}", request.email(), e.getMessage(), e);
            throw e;
        }
    }

}