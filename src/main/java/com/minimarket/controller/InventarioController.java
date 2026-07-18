package com.minimarket.controller;

import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;
import com.minimarket.service.InventarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    @ApiResponse(responseCode = "200", description = "Lista de movimientos")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public CollectionModel<EntityModel<InventarioDto>> listarMovimientosDeInventario() {
        List<EntityModel<InventarioDto>> models = inventarioService.findAll()
                .stream().map(this::toModel).toList();
        return CollectionModel.of(models,
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por ID",
               description = "Retorna un movimiento de inventario según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Movimiento encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    public ResponseEntity<EntityModel<InventarioDto>> obtenerMovimientoPorId(
            @Parameter(name = "id", description = "ID del movimiento de inventario", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        InventarioDto dto = inventarioService.findById(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento de inventario",
               description = "Registra una entrada o salida de stock. Tipos válidos: 'Entrada', 'Salida'. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Movimiento registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. tipo incorrecto, cantidad ≤ 0)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    public EntityModel<InventarioDto> registrarMovimiento(@RequestBody InventarioRequestDto request) {
        return toModel(inventarioService.save(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento de inventario",
               description = "Actualiza los datos de un movimiento existente. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Movimiento actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    public ResponseEntity<EntityModel<InventarioDto>> actualizarMovimiento(
            @Parameter(name = "id", description = "ID del movimiento a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody InventarioRequestDto request) {
        return inventarioService.update(id, request)
                .map(dto -> ResponseEntity.ok(toModel(dto)))
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

    private EntityModel<InventarioDto> toModel(InventarioDto dto) {
        EntityModel<InventarioDto> model = EntityModel.of(dto,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario"));
        if (dto.getProductoId() != null) {
            model.add(linkTo(methodOn(ProductoController.class)
                    .obtenerProductoPorId(dto.getProductoId())).withRel("producto"));
        }
        return model;
    }
}
