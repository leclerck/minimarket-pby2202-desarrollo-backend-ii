package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.repository.RolRepository;
import com.minimarket.service.RolService;
import org.springframework.stereotype.Service;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Rol findByNombre(String nombre) {
        return rolRepository.findByNombre(nombre).orElse(null);
    }
}
