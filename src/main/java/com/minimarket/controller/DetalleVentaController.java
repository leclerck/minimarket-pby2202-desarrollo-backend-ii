package com.minimarket.controller;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.DetalleVentaRequestDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Detalle de ventas. Misma política de acceso que VentaController.
 */
@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public List<DetalleVentaDto> listarDetalleVentas() {
        return dtoMapper.toDetalleVentaDtos(detalleVentaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<DetalleVentaDto> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        return (detalleVenta != null) ? ResponseEntity.ok(dtoMapper.toDto(detalleVenta)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public DetalleVentaDto guardarDetalleVenta(@RequestBody DetalleVentaRequestDto request) {
        return dtoMapper.toDto(detalleVentaService.save(dtoMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<DetalleVentaDto> actualizarDetalleVenta(@PathVariable Long id, @RequestBody DetalleVentaRequestDto request) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente != null) {
            dtoMapper.applyDetalleVentaRequest(existente, request);
            return ResponseEntity.ok(dtoMapper.toDto(detalleVentaService.save(existente)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
