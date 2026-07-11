package com.minimarket.dto.request;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CarritoRequestDto {

    private Long usuarioId;
    private Long productoId;
    private Integer cantidad;

}
