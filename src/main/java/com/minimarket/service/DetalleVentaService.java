package com.minimarket.service;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.request.DetalleVentaRequestDto;

import java.util.List;
import java.util.Optional;

public interface DetalleVentaService {
    List<DetalleVentaDto> findAll();
    DetalleVentaDto findById(Long id);
    DetalleVentaDto save(DetalleVentaRequestDto request);
    Optional<DetalleVentaDto> update(Long id, DetalleVentaRequestDto request);
    void deleteById(Long id);
    List<DetalleVentaDto> findByVentaId(Long ventaId);
}
