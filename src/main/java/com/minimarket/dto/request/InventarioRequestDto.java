package com.minimarket.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InventarioRequestDto {

    private Long productoId;
    private Integer cantidad;
    private String tipoMovimiento;
    private LocalDate fechaMovimiento;

}
