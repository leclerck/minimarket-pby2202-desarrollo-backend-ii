package com.minimarket.service;

import com.minimarket.dto.ProductoDto;
import com.minimarket.dto.request.ProductoRequestDto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<ProductoDto> findAll();
    ProductoDto findById(Long id);
    ProductoDto save(ProductoRequestDto request);
    Optional<ProductoDto> update(Long id, ProductoRequestDto request);
    void deleteById(Long id);
    List<ProductoDto> findByCategoriaId(Long categoriaId);
}
