package com.minimarket.service.impl;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.request.CategoriaRequestDto;
import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.service.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final DtoMapper dtoMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, DtoMapper dtoMapper) {
        this.categoriaRepository = categoriaRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CategoriaDto> findAll() {
        return dtoMapper.toCategoriaDtos(categoriaRepository.findAll());
    }

    @Override
    public CategoriaDto findById(Long id) {
        Categoria c = categoriaRepository.findById(id).orElse(null);
        return c != null ? dtoMapper.toDto(c) : null;
    }

    @Override
    public CategoriaDto save(CategoriaRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        Categoria categoria = dtoMapper.toEntity(request);
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        return dtoMapper.toDto(categoriaRepository.save(categoria));
    }

    @Override
    public Optional<CategoriaDto> update(Long id, CategoriaRequestDto request) {
        return categoriaRepository.findById(id).map(existing -> {
            dtoMapper.applyCategoriaRequest(existing, request);
            return dtoMapper.toDto(categoriaRepository.save(existing));
        });
    }

    @Override
    public void deleteById(Long id) {
        categoriaRepository.deleteById(id);
    }
}
