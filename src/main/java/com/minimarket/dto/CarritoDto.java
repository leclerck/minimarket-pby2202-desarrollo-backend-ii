package com.minimarket.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Relation(collectionRelation = "carritos", itemRelation = "carrito")
public class CarritoDto {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;

}
