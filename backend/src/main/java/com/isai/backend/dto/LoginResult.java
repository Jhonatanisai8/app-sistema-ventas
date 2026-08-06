package com.isai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResult {
    private AuthResponse authResponse;
    private ResponseCookie refreshTokenCookie;
}
