package com.minimarket.service.impl;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.request.DetalleVentaRequestDto;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.DetalleVentaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleVentaServiceImpl implements DetalleVentaService {

    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final DtoMapper dtoMapper;

    public DetalleVentaServiceImpl(DetalleVentaRepository detalleVentaRepository,
                                   ProductoRepository productoRepository,
                                   DtoMapper dtoMapper) {
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<DetalleVentaDto> findAll() {
        return dtoMapper.toDetalleVentaDtos(detalleVentaRepository.findAll());
    }

    @Override
    public DetalleVentaDto findById(Long id) {
        DetalleVenta d = detalleVentaRepository.findById(id).orElse(null);
        return d != null ? dtoMapper.toDto(d) : null;
    }

    @Override
    public DetalleVentaDto save(DetalleVentaRequestDto request) {
        DetalleVenta detalleVenta = dtoMapper.toEntity(request);
        if (detalleVenta.getCantidad() == null || detalleVenta.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        Producto producto = detalleVenta.getProducto();
        if (producto == null) {
            throw new IllegalArgumentException("El detalle de venta debe tener un producto asociado");
        }
        if (producto.getStock() < detalleVenta.getCantidad()) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
        }
        producto.setStock(producto.getStock() - detalleVenta.getCantidad());
        productoRepository.save(producto);
        return dtoMapper.toDto(detalleVentaRepository.save(detalleVenta));
    }

    @Override
    public Optional<DetalleVentaDto> update(Long id, DetalleVentaRequestDto request) {
        return detalleVentaRepository.findById(id).map(existing -> {
            dtoMapper.applyDetalleVentaRequest(existing, request);
            return dtoMapper.toDto(detalleVentaRepository.save(existing));
        });
    }

    @Override
    public void deleteById(Long id) {
        detalleVentaRepository.deleteById(id);
    }

    @Override
    public List<DetalleVentaDto> findByVentaId(Long ventaId) {
        return dtoMapper.toDetalleVentaDtos(detalleVentaRepository.findByVentaId(ventaId));
    }
}
