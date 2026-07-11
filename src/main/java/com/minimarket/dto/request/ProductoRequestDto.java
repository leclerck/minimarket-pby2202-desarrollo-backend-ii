package com.minimarket.dto.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductoRequestDto {

    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private Long categoriaId;

}
