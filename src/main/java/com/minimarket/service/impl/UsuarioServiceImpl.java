package com.minimarket.service.impl;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.UsuarioDto;
import com.minimarket.dto.request.UsuarioRequestDto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              DtoMapper dtoMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<UsuarioDto> findAll() {
        return dtoMapper.toUsuarioDtos(usuarioRepository.findAll());
    }

    @Override
    public UsuarioDto findById(Long id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        return u != null ? dtoMapper.toDto(u) : null;
    }

    @Override
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    @Override
    public UsuarioDto save(UsuarioRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        Usuario usuario = dtoMapper.toEntity(request);
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El username es obligatorio");
        }
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return dtoMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public Optional<UsuarioDto> update(Long id, UsuarioRequestDto request) {
        return usuarioRepository.findById(id).map(existing -> {
            dtoMapper.applyUsuarioRequest(existing, request);
            if (existing.getPassword() != null && !existing.getPassword().startsWith("$2a$")) {
                existing.setPassword(passwordEncoder.encode(existing.getPassword()));
            }
            return dtoMapper.toDto(usuarioRepository.save(existing));
        });
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
