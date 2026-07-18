package com.minimarket.controller;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.request.DetalleVentaRequestDto;
import com.minimarket.service.DetalleVentaService;

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
    @ApiResponse(responseCode = "200", description = "Lista de detalles de venta")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public CollectionModel<EntityModel<DetalleVentaDto>> listarDetalleVentas() {
        List<EntityModel<DetalleVentaDto>> models = detalleVentaService.findAll()
                .stream().map(this::toModel).toList();
        return CollectionModel.of(models,
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Obtener detalle de venta por ID",
               description = "Retorna un detalle de venta según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Detalle encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    public ResponseEntity<EntityModel<DetalleVentaDto>> obtenerDetalleVentaPorId(
            @Parameter(name = "id", description = "ID del detalle de venta", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        DetalleVentaDto dto = detalleVentaService.findById(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CAJERO')")
    @Operation(summary = "Registrar detalle de venta",
               description = "Agrega una línea de producto a una venta. Descuenta stock automáticamente. Solo rol CAJERO.")
    @ApiResponse(responseCode = "200", description = "Detalle registrado y stock actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. stock insuficiente, cantidad ≤ 0)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (solo CAJERO puede registrar detalles)", content = @Content)
    public EntityModel<DetalleVentaDto> guardarDetalleVenta(@RequestBody DetalleVentaRequestDto request) {
        return toModel(detalleVentaService.save(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Actualizar detalle de venta",
               description = "Actualiza los datos de un detalle de venta existente. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Detalle actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    public ResponseEntity<EntityModel<DetalleVentaDto>> actualizarDetalleVenta(
            @Parameter(name = "id", description = "ID del detalle a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody DetalleVentaRequestDto request) {
        return detalleVentaService.update(id, request)
                .map(dto -> ResponseEntity.ok(toModel(dto)))
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

    private EntityModel<DetalleVentaDto> toModel(DetalleVentaDto dto) {
        EntityModel<DetalleVentaDto> model = EntityModel.of(dto,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"));
        if (dto.getVentaId() != null) {
            model.add(linkTo(methodOn(VentaController.class)
                    .obtenerVentaPorId(dto.getVentaId())).withRel("venta"));
        }
        if (dto.getProductoId() != null) {
            model.add(linkTo(methodOn(ProductoController.class)
                    .obtenerProductoPorId(dto.getProductoId())).withRel("producto"));
        }
        return model;
    }
}
