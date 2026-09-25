package com.coworking.api.domain.dto;

import com.coworking.api.domain.enums.RoleEnum;

public record UserResponse(

        Long id,
        String name,
        String email,
        RoleEnum role

) {}
