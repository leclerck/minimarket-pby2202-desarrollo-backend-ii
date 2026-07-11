package com.minimarket.security.model;

import com.minimarket.entity.Usuario;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import java.util.Collections;

import java.util.stream.Collectors;

/**
 * 
 * Adaptador que convierte la entidad Usuario al formato UserDetails de Spring
 * Security.
 * 
 * Agrega el prefijo ROLE_ requerido por @PreAuthorize("hasRole('...')").
 * 
 */

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {

        this.usuario = usuario;

    }

    @Override

    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (usuario.getRoles() == null) {

            return Collections.emptyList();

        }

        // Ejemplo: rol "ADMIN" en BD → authority "ROLE_ADMIN" para Spring Security

        return usuario.getRoles().stream()

                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))

                .collect(Collectors.toList());

    }

    @Override

    public String getPassword() {

        return usuario.getPassword();

    }

    @Override

    public String getUsername() {

        return usuario.getUsername();

    }

    @Override

    public boolean isAccountNonExpired() {

        return true;

    }

    @Override

    public boolean isAccountNonLocked() {

        return true;

    }

    @Override

    public boolean isCredentialsNonExpired() {

        return true;

    }

    @Override

    public boolean isEnabled() {

        return true;

    }

}
