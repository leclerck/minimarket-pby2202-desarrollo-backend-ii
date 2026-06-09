package com.minimarket.controller;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.CategoriaRequestDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Categorías de productos. GET público; escritura solo para STAFF y ADMIN.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    public List<CategoriaDto> listarCategorias() {
        return dtoMapper.toCategoriaDtos(categoriaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDto> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(dtoMapper.toDto(categoria)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public CategoriaDto guardarCategoria(@RequestBody CategoriaRequestDto request) {
        return dtoMapper.toDto(categoriaService.save(dtoMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<CategoriaDto> actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaRequestDto request) {
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            dtoMapper.applyCategoriaRequest(categoriaExistente, request);
            return ResponseEntity.ok(dtoMapper.toDto(categoriaService.save(categoriaExistente)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
