package com.minimarket.service;

import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;

import java.util.List;
import java.util.Optional;

public interface InventarioService {
    List<InventarioDto> findAll();
    InventarioDto findById(Long id);
    InventarioDto save(InventarioRequestDto request);
    Optional<InventarioDto> update(Long id, InventarioRequestDto request);
    void deleteById(Long id);
    List<InventarioDto> findByProductoId(Long productoId);
}
