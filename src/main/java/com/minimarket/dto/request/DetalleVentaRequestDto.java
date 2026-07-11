package com.minimarket.dto.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DetalleVentaRequestDto {

    private Long ventaId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precio;

}
