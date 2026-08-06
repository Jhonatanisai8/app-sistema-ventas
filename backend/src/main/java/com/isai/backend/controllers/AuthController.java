package com.isai.backend.controllers;

import com.isai.backend.domain.entities.Rol;
import com.isai.backend.domain.entities.Usuario;
import com.isai.backend.domain.enums.RolUsuario;
import com.isai.backend.dto.AuthRequest;
import com.isai.backend.dto.AuthResponse;
import com.isai.backend.dto.RegisterRequest;
import com.isai.backend.repositories.RolRepository;
import com.isai.backend.repositories.UsuarioRepository;
import com.isai.backend.security.JwtService;
import com.isai.backend.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo ya está registrado");
        }

        Set<Rol> userRoles = new HashSet<>();
        RolUsuario mainEnumRole = RolUsuario.VENDEDOR; // Default enum role

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                String normalizedRole = roleName.toUpperCase();
                Rol rol = rolRepository.findByNombre(normalizedRole)
                        .orElseGet(() -> {
                            Rol newRol = Rol.builder()
                                    .nombre(normalizedRole)
                                    .permisos(new HashSet<>())
                                    .build();
                            return rolRepository.save(newRol);
                        });
                userRoles.add(rol);

                // Set the enum role based on matching names if possible
                try {
                    mainEnumRole = RolUsuario.valueOf(normalizedRole);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } else {
            Rol defaultRol = rolRepository.findByNombre("VENDEDOR")
                    .orElseGet(() -> {
                        Rol newRol = Rol.builder()
                                .nombre("VENDEDOR")
                                .permisos(new HashSet<>())
                                .build();
                        return rolRepository.save(newRol);
                    });
            userRoles.add(defaultRol);
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .rol(mainEnumRole)
                .roles(userRoles)
                .isEnabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena()));

        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        ResponseCookie cookie = jwtService.generateRefreshTokenCookie(refreshToken);

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Usuario user = principal.getUsuario();

        AuthResponse authResponse = AuthResponse.builder()
                .tokenAcceso(accessToken)
                .id(user.getId())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .correo(user.getCorreo())
                .roles(roles)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refreshToken = jwtService.extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de refresco faltante");
        }

        try {
            String correo = jwtService.extractUsername(refreshToken);
            if (correo != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
                if (jwtService.isTokenValid(refreshToken, userDetails)) {
                    String newAccessToken = jwtService.generateToken(userDetails);

                    UsuarioPrincipal principal = (UsuarioPrincipal) userDetails;
                    List<String> roles = principal.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());

                    Usuario user = principal.getUsuario();

                    AuthResponse authResponse = AuthResponse.builder()
                            .tokenAcceso(newAccessToken)
                            .id(user.getId())
                            .nombre(user.getNombre())
                            .apellido(user.getApellido())
                            .correo(user.getCorreo())
                            .roles(roles)
                            .build();

                    return ResponseEntity.ok(authResponse);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de refresco inválido o expirado");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de refresco inválido");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = jwtService.getCleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Sesión cerrada exitosamente");
    }
}
