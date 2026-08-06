package com.isai.backend.services;

import com.isai.backend.dto.AuthRequest;
import com.isai.backend.dto.AuthResponse;
import com.isai.backend.dto.LoginResult;
import com.isai.backend.dto.RegisterRequest;
import org.springframework.http.ResponseCookie;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResult login(AuthRequest request);

    AuthResponse refresh(String refreshToken);

    ResponseCookie logout();
}
