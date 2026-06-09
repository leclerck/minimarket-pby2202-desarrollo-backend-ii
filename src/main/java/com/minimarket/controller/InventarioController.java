package com.minimarket.controller;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.InventarioRequestDto;
import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Movimientos de inventario. Acceso restringido a STAFF y ADMIN.
 */
@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    public List<InventarioDto> listarMovimientosDeInventario() {
        return dtoMapper.toInventarioDtos(inventarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioDto> obtenerMovimientoPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(dtoMapper.toDto(inventario)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public InventarioDto registrarMovimiento(@RequestBody InventarioRequestDto request) {
        return dtoMapper.toDto(inventarioService.save(dtoMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioDto> actualizarMovimiento(@PathVariable Long id, @RequestBody InventarioRequestDto request) {
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            dtoMapper.applyInventarioRequest(existente, request);
            return ResponseEntity.ok(dtoMapper.toDto(inventarioService.save(existente)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
