package com.minimarket.controller;

import com.minimarket.dto.UsuarioDto;
import com.minimarket.dto.request.UsuarioRequestDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;
/**
 * Gestión de usuarios. Acceso restringido exclusivamente al rol ADMIN.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final DtoMapper dtoMapper;

    public UsuarioController(UsuarioService usuarioService, DtoMapper dtoMapper) {
        this.usuarioService = usuarioService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    public List<UsuarioDto> listarUsuarios() {
        return dtoMapper.toUsuarioDtos(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(u -> ResponseEntity.ok(dtoMapper.toDto(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public UsuarioDto guardarUsuario(@RequestBody UsuarioRequestDto request) {
        Usuario usuario = dtoMapper.toEntity(request);
        return dtoMapper.toDto(usuarioService.save(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDto> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDto request) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            dtoMapper.applyUsuarioRequest(usuario, request);
            return ResponseEntity.ok(dtoMapper.toDto(usuarioService.save(usuario)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
