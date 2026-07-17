package com.minimarket.controller;

import com.minimarket.dto.VentaDto;
import com.minimarket.dto.request.VentaRequestDto;
import com.minimarket.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ventas. Lectura para CAJERO/ADMIN; escritura solo para CAJERO.
 */
@Tag(name = "Ventas", description = "Registro y consulta de ventas")
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar todas las ventas",
               description = "Retorna el historial completo de ventas. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de ventas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public List<VentaDto> listarVentas() {
        return ventaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener venta por ID",
               description = "Retorna una venta según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Venta encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    public ResponseEntity<VentaDto> obtenerVentaPorId(
            @Parameter(name = "id", description = "ID de la venta", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        VentaDto dto = ventaService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CAJERO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar venta",
               description = "Registra una nueva venta. Solo el rol CAJERO puede crear ventas.")
    @ApiResponse(responseCode = "200", description = "Venta registrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. usuarioId inexistente)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (solo CAJERO puede registrar ventas)", content = @Content)
    public VentaDto guardarVenta(@RequestBody VentaRequestDto request) {
        return ventaService.save(request);
    }
}
