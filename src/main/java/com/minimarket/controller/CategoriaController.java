package com.minimarket.controller;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.request.CategoriaRequestDto;
import com.minimarket.service.CategoriaService;

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
    @Operation(summary = "Listar todas las categorías",
               description = "Retorna todas las categorías de productos. Endpoint público, no requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDto.class)))
    public List<CategoriaDto> listarCategorias() {
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID",
               description = "Retorna una categoría según su ID. Endpoint público, no requiere autenticación.")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDto.class)))
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<CategoriaDto> obtenerCategoriaPorId(
            @Parameter(name = "id", description = "ID de la categoría", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        CategoriaDto dto = categoriaService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear categoría",
               description = "Crea una nueva categoría. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Categoría creada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. nombre vacío)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    public CategoriaDto guardarCategoria(@RequestBody CategoriaRequestDto request) {
        return categoriaService.save(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar categoría",
               description = "Actualiza el nombre de una categoría existente. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDto.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<CategoriaDto> actualizarCategoria(
            @Parameter(name = "id", description = "ID de la categoría a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody CategoriaRequestDto request) {
        return categoriaService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar categoría",
               description = "Elimina una categoría. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(name = "id", description = "ID de la categoría a eliminar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        if (categoriaService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
