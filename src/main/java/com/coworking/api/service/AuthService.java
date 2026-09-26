package com.coworking.api.service;

import com.coworking.api.domain.dto.AuthResponse;
import com.coworking.api.domain.dto.LoginRequest;
import com.coworking.api.domain.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

}
