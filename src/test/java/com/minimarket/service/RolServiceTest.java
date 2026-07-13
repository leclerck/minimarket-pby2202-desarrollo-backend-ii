package com.minimarket.service;

import com.minimarket.entity.Rol;
import com.minimarket.repository.RolRepository;
import com.minimarket.service.impl.RolServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de RolService")
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    // --- findByNombre ---

    @Test
    @DisplayName("Buscar rol existente retorna el rol")
    void buscarPorNombre_rolExistente_retornaRol() {
        Rol admin = new Rol("ADMIN");
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(admin));

        Rol result = rolService.findByNombre("ADMIN");

        assertThat(result).isEqualTo(admin);
    }

    @Test
    @DisplayName("Buscar rol inexistente retorna null")
    void buscarPorNombre_rolInexistente_retornaNull() {
        when(rolRepository.findByNombre("DESCONOCIDO")).thenReturn(Optional.empty());

        Rol result = rolService.findByNombre("DESCONOCIDO");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Buscar rol con nombre nulo retorna null")
    void buscarPorNombre_nombreNulo_retornaNull() {
        when(rolRepository.findByNombre(null)).thenReturn(Optional.empty());

        Rol result = rolService.findByNombre(null);

        assertThat(result).isNull();
    }

    @ParameterizedTest(name = "rol \"{0}\" retorna resultado del repositorio")
    @ValueSource(strings = {"ADMIN", "CAJERO"})
    @DisplayName("Nombres de roles válidos del sistema consultan al repositorio")
    void nombresDeRol_validos_consultanRepositorio(String nombre) {
        Rol rol = new Rol(nombre);
        when(rolRepository.findByNombre(nombre)).thenReturn(Optional.of(rol));

        Rol result = rolService.findByNombre(nombre);

        assertThat(result).isEqualTo(rol);
        verify(rolRepository).findByNombre(nombre);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(rolRepository.findByNombre(anyString()))
                .thenThrow(new RuntimeException("BD no disponible"));

        assertThatThrownBy(() -> rolService.findByNombre("ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("BD no disponible");
    }
}
