package com.minimarket.dto;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Relation(collectionRelation = "usuarios", itemRelation = "usuario")
public class UsuarioDto {

    private Long id;
    private String username;
    private Set<RolDto> roles;

}
