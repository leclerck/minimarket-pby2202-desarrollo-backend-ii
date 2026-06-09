package com.minimarket.controller;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.VentaDto;
import com.minimarket.dto.VentaRequestDto;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ventas. Lectura para STAFF/ADMIN; creación también permitida para USER (clientes).
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public List<VentaDto> listarVentas() {
        return dtoMapper.toVentaDtos(ventaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<VentaDto> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        return (venta != null) ? ResponseEntity.ok(dtoMapper.toDto(venta)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public VentaDto guardarVenta(@RequestBody VentaRequestDto request) {
        return dtoMapper.toDto(ventaService.save(dtoMapper.toEntity(request)));
    }
}
