package com.minimarket.dto;

import java.util.Set;

public class UsuarioDto {

    private Long id;
    private String username;
    private Set<RolDto> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<RolDto> getRoles() {
        return roles;
    }

    public void setRoles(Set<RolDto> roles) {
        this.roles = roles;
    }
}
