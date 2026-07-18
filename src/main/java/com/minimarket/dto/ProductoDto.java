package com.minimarket.dto;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Relation(collectionRelation = "productos", itemRelation = "producto")
public class ProductoDto {

    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private Long categoriaId;
    private String categoriaNombre;

}
