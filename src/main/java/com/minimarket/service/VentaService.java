package com.minimarket.service;

import com.minimarket.dto.VentaDto;
import com.minimarket.dto.request.VentaRequestDto;

import java.util.List;

public interface VentaService {
    List<VentaDto> findAll();
    VentaDto findById(Long id);
    VentaDto save(VentaRequestDto request);
    List<VentaDto> findByUsuarioId(Long usuarioId);
}
