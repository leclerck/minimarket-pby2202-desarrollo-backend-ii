package com.minimarket.controller;

import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;
import com.minimarket.service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Movimientos de inventario. Acceso restringido a CAJERO y ADMIN.
 */
@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<InventarioDto> listarMovimientosDeInventario() {
        return inventarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioDto> obtenerMovimientoPorId(@PathVariable Long id) {
        InventarioDto dto = inventarioService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public InventarioDto registrarMovimiento(@RequestBody InventarioRequestDto request) {
        return inventarioService.save(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioDto> actualizarMovimiento(@PathVariable Long id,
                                                              @RequestBody InventarioRequestDto request) {
        return inventarioService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        if (inventarioService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
