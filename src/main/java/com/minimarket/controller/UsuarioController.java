package com.minimarket.controller;

import com.minimarket.dto.UsuarioDto;
import com.minimarket.dto.request.UsuarioRequestDto;
import com.minimarket.service.UsuarioService;

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
 * Gestión de usuarios. Acceso restringido exclusivamente al rol ADMIN.
 */
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema. Solo accesible por ADMIN.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios",
               description = "Retorna la lista de todos los usuarios registrados. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    public CollectionModel<EntityModel<UsuarioDto>> listarUsuarios() {
        List<EntityModel<UsuarioDto>> models = usuarioService.findAll()
                .stream().map(this::toModel).toList();
        return CollectionModel.of(models,
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID",
               description = "Retorna un usuario según su ID. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    public ResponseEntity<EntityModel<UsuarioDto>> obtenerUsuarioPorId(
            @Parameter(name = "id", description = "ID del usuario", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        UsuarioDto dto = usuarioService.findById(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear usuario",
               description = "Crea un nuevo usuario en el sistema. La contraseña se almacena con BCrypt. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario creado (la respuesta nunca incluye la contraseña)")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. username vacío)", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    public EntityModel<UsuarioDto> guardarUsuario(@RequestBody UsuarioRequestDto request) {
        return toModel(usuarioService.save(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario",
               description = "Actualiza los datos de un usuario existente. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    public ResponseEntity<EntityModel<UsuarioDto>> actualizarUsuario(
            @Parameter(name = "id", description = "ID del usuario a actualizar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id,
            @RequestBody UsuarioRequestDto request) {
        return usuarioService.update(id, request)
                .map(dto -> ResponseEntity.ok(toModel(dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario",
               description = "Elimina un usuario del sistema. Requiere rol ADMIN.")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado", content = @Content)
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sin permiso (requiere ADMIN)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(name = "id", description = "ID del usuario a eliminar", example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        if (usuarioService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UsuarioDto> toModel(UsuarioDto dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"));
    }
}
