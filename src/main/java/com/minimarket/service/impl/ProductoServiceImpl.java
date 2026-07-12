package com.minimarket.service.impl;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.ProductoDto;
import com.minimarket.dto.request.ProductoRequestDto;
import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final DtoMapper dtoMapper;

    public ProductoServiceImpl(ProductoRepository productoRepository, DtoMapper dtoMapper) {
        this.productoRepository = productoRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<ProductoDto> findAll() {
        return dtoMapper.toProductoDtos(productoRepository.findAll());
    }

    @Override
    public ProductoDto findById(Long id) {
        Producto p = productoRepository.findById(id).orElse(null);
        return p != null ? dtoMapper.toDto(p) : null;
    }

    @Override
    public ProductoDto save(ProductoRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        Producto producto = dtoMapper.toEntity(request);
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        return dtoMapper.toDto(productoRepository.save(producto));
    }

    @Override
    public Optional<ProductoDto> update(Long id, ProductoRequestDto request) {
        return productoRepository.findById(id).map(existing -> {
            dtoMapper.applyProductoRequest(existing, request);
            return dtoMapper.toDto(productoRepository.save(existing));
        });
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<ProductoDto> findByCategoriaId(Long categoriaId) {
        return dtoMapper.toProductoDtos(productoRepository.findByCategoriaId(categoriaId));
    }
}
