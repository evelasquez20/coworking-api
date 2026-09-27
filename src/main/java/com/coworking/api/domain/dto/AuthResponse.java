package com.coworking.api.domain.dto;

public record AuthResponse(
        String token,                        // El Token JWT generado para que el cliente lo guarde
        String email,                        // Email del usuario autenticado
        String role                          // Rol del usuario (ROLE_ADMIN o ROLE_USER)
) {}
