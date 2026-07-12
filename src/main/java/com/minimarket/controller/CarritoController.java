package com.minimarket.controller;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.request.CarritoRequestDto;
import com.minimarket.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Carrito de compras. Acceso restringido a CAJERO y ADMIN.
 */
@RestController
@RequestMapping("/api/carrito")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public List<CarritoDto> listarCarrito() {
        return carritoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoDto> obtenerCarritoPorId(@PathVariable Long id) {
        CarritoDto dto = carritoService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public CarritoDto agregarProductoAlCarrito(@RequestBody CarritoRequestDto request) {
        return carritoService.save(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoDto> actualizarCarrito(@PathVariable Long id,
                                                        @RequestBody CarritoRequestDto request) {
        return carritoService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        if (carritoService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        carritoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
