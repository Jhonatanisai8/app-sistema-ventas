package com.isai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String tokenAcceso;
    @Builder.Default
    private String tipoToken = "Bearer";
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private List<String> roles;
}
