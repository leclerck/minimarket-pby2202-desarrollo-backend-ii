package com.minimarket.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CarritoDto {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;

}
