package com.minimarket.dto;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UsuarioDto {

    private Long id;
    private String username;
    private Set<RolDto> roles;

}
