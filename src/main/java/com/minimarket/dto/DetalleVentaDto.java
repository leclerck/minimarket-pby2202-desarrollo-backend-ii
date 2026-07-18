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
@Relation(collectionRelation = "detalleventas", itemRelation = "detalleventa")
public class DetalleVentaDto {

    private Long id;
    private Long ventaId;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precio;

}
