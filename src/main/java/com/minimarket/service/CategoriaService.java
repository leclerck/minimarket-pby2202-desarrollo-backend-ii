package com.minimarket.service;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.request.CategoriaRequestDto;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {
    List<CategoriaDto> findAll();
    CategoriaDto findById(Long id);
    CategoriaDto save(CategoriaRequestDto request);
    Optional<CategoriaDto> update(Long id, CategoriaRequestDto request);
    void deleteById(Long id);
}
