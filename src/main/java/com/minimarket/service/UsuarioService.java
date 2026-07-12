package com.minimarket.service;

import com.minimarket.dto.UsuarioDto;
import com.minimarket.dto.request.UsuarioRequestDto;
import com.minimarket.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<UsuarioDto> findAll();
    UsuarioDto findById(Long id);
    Usuario findByUsername(String username);
    UsuarioDto save(UsuarioRequestDto request);
    Optional<UsuarioDto> update(Long id, UsuarioRequestDto request);
    void deleteById(Long id);
}
