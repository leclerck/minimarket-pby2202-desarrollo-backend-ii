package com.minimarket.controller;

import com.minimarket.dto.ProductoDto;
import com.minimarket.dto.request.ProductoRequestDto;
import com.minimarket.service.ProductoService;

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
 * Catálogo de productos. GET público; escritura solo para ADMIN.
 */
@Tag(name = "Productos", description = "Catálogo de productos del minimarket")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos",
               description = "Retorna el catálogo completo de productos. Endpoint público, no requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    public CollectionModel<EntityModel<ProductoDto>> listarProductos() {
        List<EntityModel<ProductoDto>> models = productoService.findAll()
                .stream().map(this::toModel).toList();
        return CollectionModel.of(models,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID",
               description = "Retorna un producto según su ID. Endpoint público, no requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<EntityModel<ProductoDto>> obtenerProductoPorId(
            @Parameter(name = "id", description = "ID del producto", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        ProductoDto dto = productoService.findById(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear producto",
               description = "Crea un nuevo producto en el catálogo. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. precio ≤ 0, nombre vacío)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    public EntityModel<ProductoDto> guardarProducto(@RequestBody ProductoRequestDto request) {
        return toModel(productoService.save(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar producto",
               description = "Actualiza los datos de un producto existente. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<EntityModel<ProductoDto>> actualizarProducto(
            @Parameter(name = "id", description = "ID del producto a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody ProductoRequestDto request) {
        return productoService.update(id, request)
                .map(dto -> ResponseEntity.ok(toModel(dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar producto",
               description = "Elimina un producto del catálogo. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "204", description = "Producto eliminado", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(name = "id", description = "ID del producto a eliminar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        if (productoService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ProductoDto> toModel(ProductoDto dto) {
        EntityModel<ProductoDto> model = EntityModel.of(dto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));
        if (dto.getCategoriaId() != null) {
            model.add(linkTo(methodOn(CategoriaController.class)
                    .obtenerCategoriaPorId(dto.getCategoriaId())).withRel("categoria"));
        }
        return model;
    }
}
