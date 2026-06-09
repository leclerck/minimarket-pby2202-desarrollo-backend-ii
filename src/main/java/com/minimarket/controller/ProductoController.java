package com.minimarket.controller;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.ProductoDto;
import com.minimarket.dto.ProductoRequestDto;
import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catálogo de productos. GET público; escritura solo para STAFF y ADMIN.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    public List<ProductoDto> listarProductos() {
        return dtoMapper.toProductoDtos(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(dtoMapper.toDto(producto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ProductoDto guardarProducto(@RequestBody ProductoRequestDto request) {
        return dtoMapper.toDto(productoService.save(dtoMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ProductoDto> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequestDto request) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            dtoMapper.applyProductoRequest(productoExistente, request);
            return ResponseEntity.ok(dtoMapper.toDto(productoService.save(productoExistente)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
