package com.minimarket.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.hateoas.server.core.Relation;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Relation(collectionRelation = "categorias", itemRelation = "categoria")
public class CategoriaDto {

    private Long id;
    private String nombre;

}
