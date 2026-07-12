package com.minimarket.service.impl;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.VentaDto;
import com.minimarket.dto.request.VentaRequestDto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.VentaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final DtoMapper dtoMapper;

    public VentaServiceImpl(VentaRepository ventaRepository, DtoMapper dtoMapper) {
        this.ventaRepository = ventaRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<VentaDto> findAll() {
        return dtoMapper.toVentaDtos(ventaRepository.findAll());
    }

    @Override
    public VentaDto findById(Long id) {
        Venta v = ventaRepository.findById(id).orElse(null);
        return v != null ? dtoMapper.toDto(v) : null;
    }

    @Override
    public VentaDto save(VentaRequestDto request) {
        Venta venta = dtoMapper.toEntity(request);
        if (venta.getUsuario() == null) {
            throw new IllegalArgumentException("La venta debe tener un usuario asociado");
        }
        return dtoMapper.toDto(ventaRepository.save(venta));
    }

    @Override
    public List<VentaDto> findByUsuarioId(Long usuarioId) {
        return dtoMapper.toVentaDtos(ventaRepository.findByUsuarioId(usuarioId));
    }
}
