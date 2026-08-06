package com.isai.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Acceso público: Todo el mundo puede ver esto.";
    }

    @GetMapping("/secured")
    public String securedEndpoint() {
        return "Acceso autenticado: Cualquier usuario registrado y logueado puede ver esto.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String adminEndpoint() {
        return "Acceso de Administrador: Solo los usuarios con rol ADMINISTRADOR pueden ver esto.";
    }

    @GetMapping("/vendedor")
    @PreAuthorize("hasRole('VENDEDOR')")
    public String vendedorEndpoint() {
        return "Acceso de Vendedor: Solo los usuarios con rol VENDEDOR pueden ver esto.";
    }

    @GetMapping("/permiso-lectura")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    public String permissionEndpoint() {
        return "Acceso por Permiso: Solo los usuarios con el permiso 'READ_PRIVILEGE' pueden ver esto.";
    }
}
