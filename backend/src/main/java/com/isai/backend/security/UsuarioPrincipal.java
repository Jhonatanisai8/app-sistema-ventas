package com.isai.backend.security;

import com.isai.backend.domain.entities.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (usuario.getRoles() != null) {
            usuario.getRoles().forEach(rol -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre().toUpperCase()));
                if (rol.getPermisos() != null) {
                    rol.getPermisos().forEach(permiso -> {
                        authorities.add(new SimpleGrantedAuthority(permiso.getNombrePermiso()));
                    });
                }
            });
        }

        return authorities;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return usuario.getAccountNonExpired() != null ? usuario.getAccountNonExpired() : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.getAccountNonLocked() != null ? usuario.getAccountNonLocked() : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return usuario.getCredentialsNonExpired() != null ? usuario.getCredentialsNonExpired() : true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getIsEnabled() != null ? usuario.getIsEnabled() : true;
    }
}
