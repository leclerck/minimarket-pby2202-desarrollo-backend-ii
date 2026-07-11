package com.minimarket.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class VentaDto {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private LocalDate fecha;
    private List<DetalleVentaDto> detalles;

}
