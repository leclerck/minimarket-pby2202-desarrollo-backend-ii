package com.minimarket.controller;

import com.minimarket.dto.VentaDto;
import com.minimarket.dto.request.VentaRequestDto;
import com.minimarket.service.VentaService;

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
    @ApiResponse(responseCode = "200", description = "Lista de ventas")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public CollectionModel<EntityModel<VentaDto>> listarVentas() {
        List<EntityModel<VentaDto>> models = ventaService.findAll()
                .stream().map(this::toModel).toList();
        return CollectionModel.of(models,
                linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener venta por ID",
               description = "Retorna una venta según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Venta encontrada")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    public ResponseEntity<EntityModel<VentaDto>> obtenerVentaPorId(
            @Parameter(name = "id", description = "ID de la venta", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        VentaDto dto = ventaService.findById(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CAJERO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar venta",
               description = "Registra una nueva venta. Solo el rol CAJERO puede crear ventas.")
    @ApiResponse(responseCode = "200", description = "Venta registrada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. usuarioId inexistente)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (solo CAJERO puede registrar ventas)", content = @Content)
    public EntityModel<VentaDto> guardarVenta(@RequestBody VentaRequestDto request) {
        return toModel(ventaService.save(request));
    }

    private EntityModel<VentaDto> toModel(VentaDto dto) {
        EntityModel<VentaDto> model = EntityModel.of(dto,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"));
        if (dto.getUsuarioId() != null) {
            model.add(linkTo(methodOn(UsuarioController.class)
                    .obtenerUsuarioPorId(dto.getUsuarioId())).withRel("usuario"));
        }
        return model;
    }
}
