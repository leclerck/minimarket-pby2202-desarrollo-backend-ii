package com.minimarket.controller;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.request.CategoriaRequestDto;
import com.minimarket.service.CategoriaService;

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
 * Categorías de productos. GET público; escritura solo para ADMIN.
 */
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Retorna todas las categorías de productos. Endpoint público, no requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    public CollectionModel<EntityModel<CategoriaDto>> listarCategorias() {
        List<EntityModel<CategoriaDto>> models = categoriaService.findAll()
                .stream().map(this::toModel).toList();
        return CollectionModel.of(models,
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Retorna una categoría según su ID. Endpoint público, no requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<EntityModel<CategoriaDto>> obtenerCategoriaPorId(
            @Parameter(name = "id", description = "ID de la categoría", example = "1", in = ParameterIn.PATH) @PathVariable Long id) {
        CategoriaDto dto = categoriaService.findById(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Categoría creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. nombre vacío)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    public EntityModel<CategoriaDto> guardarCategoria(@RequestBody CategoriaRequestDto request) {
        return toModel(categoriaService.save(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar categoría", description = "Actualiza el nombre de una categoría existente. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<EntityModel<CategoriaDto>> actualizarCategoria(
            @Parameter(name = "id", description = "ID de la categoría a actualizar", example = "1", in = ParameterIn.PATH) @PathVariable Long id,
            @RequestBody CategoriaRequestDto request) {
        return categoriaService.update(id, request)
                .map(dto -> ResponseEntity.ok(toModel(dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(name = "id", description = "ID de la categoría a eliminar", example = "1", in = ParameterIn.PATH) @PathVariable Long id) {
        if (categoriaService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<CategoriaDto> toModel(CategoriaDto dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(dto.getId()))
                        .withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias())
                        .withRel("categorias"));
    }
}
