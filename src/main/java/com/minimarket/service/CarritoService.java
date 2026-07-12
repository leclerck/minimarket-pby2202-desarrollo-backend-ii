package com.minimarket.service;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.request.CarritoRequestDto;

import java.util.List;
import java.util.Optional;

public interface CarritoService {
    List<CarritoDto> findAll();
    CarritoDto findById(Long id);
    CarritoDto save(CarritoRequestDto request);
    Optional<CarritoDto> update(Long id, CarritoRequestDto request);
    void deleteById(Long id);
    List<CarritoDto> findByUsuarioId(Long usuarioId);
}
