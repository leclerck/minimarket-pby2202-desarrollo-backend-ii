package com.minimarket.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VentaRequestDto {

    private Long usuarioId;
    private LocalDate fecha;

}
