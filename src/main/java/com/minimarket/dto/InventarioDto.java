package com.minimarket.dto;

import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Relation(collectionRelation = "inventarios", itemRelation = "inventario")
public class InventarioDto {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private String tipoMovimiento;
    private LocalDate fechaMovimiento;

}
