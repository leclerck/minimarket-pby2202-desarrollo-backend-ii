package com.minimarket.controller;

import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;
import com.minimarket.service.InventarioService;

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
 * Movimientos de inventario. Acceso restringido a CAJERO y ADMIN.
 */
@Tag(name = "Inventario", description = "Registro de movimientos de inventario (entradas y salidas)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    @Operation(summary = "Listar movimientos de inventario",
               description = "Retorna todos los movimientos de inventario. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de movimientos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public List<InventarioDto> listarMovimientosDeInventario() {
        return inventarioService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por ID",
               description = "Retorna un movimiento de inventario según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Movimiento encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    public ResponseEntity<InventarioDto> obtenerMovimientoPorId(
            @Parameter(name = "id", description = "ID del movimiento de inventario", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        InventarioDto dto = inventarioService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento de inventario",
               description = "Registra una entrada o salida de stock. Tipos válidos: 'Entrada', 'Salida'. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Movimiento registrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. tipo incorrecto, cantidad ≤ 0)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    public InventarioDto registrarMovimiento(@RequestBody InventarioRequestDto request) {
        return inventarioService.save(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento de inventario",
               description = "Actualiza los datos de un movimiento existente. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Movimiento actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    public ResponseEntity<InventarioDto> actualizarMovimiento(
            @Parameter(name = "id", description = "ID del movimiento a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody InventarioRequestDto request) {
        return inventarioService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento de inventario",
               description = "Elimina un movimiento de inventario según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "204", description = "Movimiento eliminado", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(name = "id", description = "ID del movimiento a eliminar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        if (inventarioService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
