package com.minimarket.controller;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.request.CarritoRequestDto;
import com.minimarket.service.CarritoService;

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
 * Carrito de compras. Acceso restringido a CAJERO y ADMIN.
 */
@Tag(name = "Carrito", description = "Gestión del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/carrito")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los ítems del carrito",
               description = "Retorna todos los ítems del carrito. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de ítems del carrito",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere CAJERO o ADMIN)", content = @Content)
    public List<CarritoDto> listarCarrito() {
        return carritoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem del carrito por ID",
               description = "Retorna un ítem del carrito según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Ítem encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoDto.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ítem no encontrado", content = @Content)
    public ResponseEntity<CarritoDto> obtenerCarritoPorId(
            @Parameter(name = "id", description = "ID del ítem del carrito", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        CarritoDto dto = carritoService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Agregar producto al carrito",
               description = "Agrega un producto al carrito de compras. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Producto agregado al carrito",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. cantidad ≤ 0 o IDs inexistentes)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    public CarritoDto agregarProductoAlCarrito(@RequestBody CarritoRequestDto request) {
        return carritoService.save(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ítem del carrito",
               description = "Actualiza los datos de un ítem existente en el carrito. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Ítem actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarritoDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ítem no encontrado", content = @Content)
    public ResponseEntity<CarritoDto> actualizarCarrito(
            @Parameter(name = "id", description = "ID del ítem a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody CarritoRequestDto request) {
        return carritoService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ítem del carrito",
               description = "Elimina un ítem del carrito según su ID. Requiere rol CAJERO o ADMIN.")
    @ApiResponse(responseCode = "204", description = "Ítem eliminado", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ítem no encontrado", content = @Content)
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(name = "id", description = "ID del ítem a eliminar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        if (carritoService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        carritoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
