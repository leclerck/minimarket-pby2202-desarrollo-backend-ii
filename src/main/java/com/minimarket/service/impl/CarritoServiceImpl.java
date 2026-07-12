package com.minimarket.service.impl;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.request.CarritoRequestDto;
import com.minimarket.entity.Carrito;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.service.CarritoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final DtoMapper dtoMapper;

    public CarritoServiceImpl(CarritoRepository carritoRepository, DtoMapper dtoMapper) {
        this.carritoRepository = carritoRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CarritoDto> findAll() {
        return dtoMapper.toCarritoDtos(carritoRepository.findAll());
    }

    @Override
    public CarritoDto findById(Long id) {
        Carrito c = carritoRepository.findById(id).orElse(null);
        return c != null ? dtoMapper.toDto(c) : null;
    }

    @Override
    public CarritoDto save(CarritoRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("El carrito no puede ser nulo");
        }
        Carrito carrito = dtoMapper.toEntity(request);
        if (carrito.getUsuario() == null) {
            throw new IllegalArgumentException("El carrito debe tener un usuario");
        }
        if (carrito.getProducto() == null) {
            throw new IllegalArgumentException("El carrito debe tener un producto");
        }
        if (carrito.getCantidad() == null || carrito.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        return dtoMapper.toDto(carritoRepository.save(carrito));
    }

    @Override
    public Optional<CarritoDto> update(Long id, CarritoRequestDto request) {
        return carritoRepository.findById(id).map(existing -> {
            dtoMapper.applyCarritoRequest(existing, request);
            return dtoMapper.toDto(carritoRepository.save(existing));
        });
    }

    @Override
    public void deleteById(Long id) {
        carritoRepository.deleteById(id);
    }

    @Override
    public List<CarritoDto> findByUsuarioId(Long usuarioId) {
        return dtoMapper.toCarritoDtos(carritoRepository.findByUsuarioId(usuarioId));
    }
}
