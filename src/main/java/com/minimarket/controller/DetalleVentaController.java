package com.minimarket.controller;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.request.DetalleVentaRequestDto;
import com.minimarket.service.DetalleVentaService;

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
 * Detalle de ventas. Misma política de acceso que VentaController.
 */
@Tag(name = "Detalle de ventas", description = "Líneas de producto dentro de una venta")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    public DetalleVentaController(DetalleVentaService detalleVentaService) {
        this.detalleVentaService = detalleVentaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Listar todos los detalles de venta",
               description = "Retorna todos los detalles de venta registrados. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de detalles de venta",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public List<DetalleVentaDto> listarDetalleVentas() {
        return detalleVentaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Obtener detalle de venta por ID",
               description = "Retorna un detalle de venta según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Detalle encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    public ResponseEntity<DetalleVentaDto> obtenerDetalleVentaPorId(
            @Parameter(name = "id", description = "ID del detalle de venta", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        DetalleVentaDto dto = detalleVentaService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CAJERO')")
    @Operation(summary = "Registrar detalle de venta",
               description = "Agrega una línea de producto a una venta. Descuenta stock automáticamente. Solo rol CAJERO.")
    @ApiResponse(responseCode = "200", description = "Detalle registrado y stock actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. stock insuficiente, cantidad ≤ 0)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (solo CAJERO puede registrar detalles)", content = @Content)
    public DetalleVentaDto guardarDetalleVenta(@RequestBody DetalleVentaRequestDto request) {
        return detalleVentaService.save(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Actualizar detalle de venta",
               description = "Actualiza los datos de un detalle de venta existente. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Detalle actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVentaDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    public ResponseEntity<DetalleVentaDto> actualizarDetalleVenta(
            @Parameter(name = "id", description = "ID del detalle a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody DetalleVentaRequestDto request) {
        return detalleVentaService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Eliminar detalle de venta",
               description = "Elimina un detalle de venta según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "204", description = "Detalle eliminado", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    public ResponseEntity<Void> eliminarDetalleVenta(
            @Parameter(name = "id", description = "ID del detalle a eliminar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        if (detalleVentaService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        detalleVentaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
