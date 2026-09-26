package com.coworking.api.service.impl;

import com.coworking.api.config.security.JwtUtils;
import com.coworking.api.domain.dto.AuthResponse;
import com.coworking.api.domain.dto.LoginRequest;
import com.coworking.api.domain.dto.RegisterRequest;
import com.coworking.api.domain.entity.User;
import com.coworking.api.domain.enums.RoleEnum;
import com.coworking.api.exception.GlobalExceptionHandler.BadRequestException;
import com.coworking.api.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.coworking.api.repository.UserRepository;
import com.coworking.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("El correo electrónico ya se encuentra registrado.");
        }

        RoleEnum role = request.role() != null ? request.role() : RoleEnum.ROLE_USER;

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .build();

        userRepository.save(user);

        String token = jwtUtils.generateToken(user);

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email proporcionado."));

        String token = jwtUtils.generateToken(user);

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

}