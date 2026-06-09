package com.minimarket.controller;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.CarritoRequestDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Carrito de compras. Accesible para clientes (USER), personal (STAFF) y administradores.
 */
@RestController
@RequestMapping("/api/carrito")
@PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    public List<CarritoDto> listarCarrito() {
        return dtoMapper.toCarritoDtos(carritoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoDto> obtenerCarritoPorId(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        return (carrito != null) ? ResponseEntity.ok(dtoMapper.toDto(carrito)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public CarritoDto agregarProductoAlCarrito(@RequestBody CarritoRequestDto request) {
        return dtoMapper.toDto(carritoService.save(dtoMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoDto> actualizarCarrito(@PathVariable Long id, @RequestBody CarritoRequestDto request) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            dtoMapper.applyCarritoRequest(existente, request);
            return ResponseEntity.ok(dtoMapper.toDto(carritoService.save(existente)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
