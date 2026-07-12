package com.minimarket.service.impl;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;
import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final DtoMapper dtoMapper;

    public InventarioServiceImpl(InventarioRepository inventarioRepository, DtoMapper dtoMapper) {
        this.inventarioRepository = inventarioRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<InventarioDto> findAll() {
        return dtoMapper.toInventarioDtos(inventarioRepository.findAll());
    }

    @Override
    public InventarioDto findById(Long id) {
        Inventario inv = inventarioRepository.findById(id).orElse(null);
        return inv != null ? dtoMapper.toDto(inv) : null;
    }

    @Override
    public InventarioDto save(InventarioRequestDto request) {
        Inventario inventario = dtoMapper.toEntity(request);
        if (inventario.getCantidad() == null || inventario.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        String tipo = inventario.getTipoMovimiento();
        if (tipo == null || (!tipo.equals("Entrada") && !tipo.equals("Salida"))) {
            throw new IllegalArgumentException("El tipo de movimiento debe ser 'Entrada' o 'Salida'");
        }
        return dtoMapper.toDto(inventarioRepository.save(inventario));
    }

    @Override
    public Optional<InventarioDto> update(Long id, InventarioRequestDto request) {
        return inventarioRepository.findById(id).map(existing -> {
            dtoMapper.applyInventarioRequest(existing, request);
            return dtoMapper.toDto(inventarioRepository.save(existing));
        });
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<InventarioDto> findByProductoId(Long productoId) {
        return dtoMapper.toInventarioDtos(inventarioRepository.findByProductoId(productoId));
    }
}
