package com.minimarket.service;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.VentaDto;
import com.minimarket.dto.request.VentaRequestDto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de VentaService")
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private VentaServiceImpl ventaService;

    // --- Auxiliares ---

    private Venta ventaValida() {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setFecha(LocalDate.now());
        venta.setUsuario(new Usuario());
        return venta;
    }

    private VentaRequestDto requestValido() {
        VentaRequestDto r = new VentaRequestDto();
        r.setUsuarioId(1L);
        r.setFecha(LocalDate.now());
        return r;
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar ventas retorna todos los registros como DTOs")
    void listarVentas_retornaListaCompleta() {
        when(ventaRepository.findAll()).thenReturn(List.of(ventaValida(), ventaValida()));
        when(dtoMapper.toVentaDtos(any())).thenReturn(List.of(new VentaDto(), new VentaDto()));

        List<VentaDto> result = ventaService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Listar ventas: repositorio vacío retorna lista vacía")
    void listarVentas_repositorioVacio_retornaListaVacia() {
        when(ventaRepository.findAll()).thenReturn(List.of());
        when(dtoMapper.toVentaDtos(any())).thenReturn(List.of());

        List<VentaDto> result = ventaService.findAll();

        assertThat(result).isEmpty();
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar venta existente retorna el DTO")
    void buscarPorId_ventaExistente_retornaDto() {
        Venta venta = ventaValida();
        VentaDto dto = new VentaDto();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(dtoMapper.toDto(venta)).thenReturn(dto);

        VentaDto result = ventaService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar venta inexistente retorna null")
    void buscarPorId_ventaInexistente_retornaNull() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThat(ventaService.findById(99L)).isNull();
    }

    @ParameterizedTest(name = "id={0}")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Buscar por ids límite retorna null")
    void buscarPorId_idsLimite_retornaNull(long id) {
        when(ventaRepository.findById(id)).thenReturn(Optional.empty());

        VentaDto result = ventaService.findById(id);

        assertThat(result).isNull();
    }

    // --- save (válido) ---

    @Test
    @DisplayName("Guardar venta con usuario válido delega al repositorio y retorna DTO")
    void guardarVenta_conUsuarioValido_guardaCorrectamente() {
        Venta venta = ventaValida();
        VentaDto dto = new VentaDto();
        when(dtoMapper.toEntity(any(VentaRequestDto.class))).thenReturn(venta);
        when(ventaRepository.save(venta)).thenReturn(venta);
        when(dtoMapper.toDto(venta)).thenReturn(dto);

        VentaDto result = ventaService.save(requestValido());

        assertThat(result).isEqualTo(dto);
        verify(ventaRepository).save(venta);
    }

    // --- save (inválidos) → verify(never()) ---

    @Test
    @DisplayName("Guardar venta sin usuario lanza excepción y no llama save")
    void guardarVenta_sinUsuario_lanzaExcepcion() {
        Venta ventaSinUsuario = ventaValida();
        ventaSinUsuario.setUsuario(null);
        when(dtoMapper.toEntity(any(VentaRequestDto.class))).thenReturn(ventaSinUsuario);

        VentaRequestDto request = requestValido();
        assertThatThrownBy(() -> ventaService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usuario");

        verify(ventaRepository, never()).save(any());
    }

    // --- findByUsuarioId ---

    @Test
    @DisplayName("Buscar por usuario retorna sus ventas como DTOs")
    void buscarPorUsuario_retornaVentasDelUsuario() {
        when(ventaRepository.findByUsuarioId(5L)).thenReturn(List.of(ventaValida()));
        when(dtoMapper.toVentaDtos(any())).thenReturn(List.of(new VentaDto()));

        List<VentaDto> result = ventaService.findByUsuarioId(5L);

        assertThat(result).hasSize(1);
        verify(ventaRepository).findByUsuarioId(5L);
    }

    @Test
    @DisplayName("Buscar por usuario sin ventas retorna lista vacía")
    void buscarPorUsuario_sinVentas_retornaListaVacia() {
        when(ventaRepository.findByUsuarioId(anyLong())).thenReturn(List.of());
        when(dtoMapper.toVentaDtos(any())).thenReturn(List.of());

        List<VentaDto> result = ventaService.findByUsuarioId(99L);

        assertThat(result).isEmpty();
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(ventaRepository.findAll()).thenThrow(new RuntimeException("Error de conexión"));

        assertThatThrownBy(() -> ventaService.findAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de conexión");
    }

    @Test
    @DisplayName("DataAccessException del repositorio se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Venta venta = ventaValida();
        when(dtoMapper.toEntity(any(VentaRequestDto.class))).thenReturn(venta);
        when(ventaRepository.save(venta))
                .thenThrow(new DataIntegrityViolationException("Fallo de integridad"));

        VentaRequestDto request = requestValido();
        assertThatThrownBy(() -> ventaService.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
