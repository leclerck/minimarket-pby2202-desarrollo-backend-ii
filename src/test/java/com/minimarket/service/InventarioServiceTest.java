package com.minimarket.service;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
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
@DisplayName("Pruebas unitarias de InventarioService")
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    // --- Auxiliares ---

    private Inventario movimientoValido(String tipo) {
        Inventario inv = new Inventario();
        inv.setId(1L);
        inv.setCantidad(10);
        inv.setTipoMovimiento(tipo);
        inv.setFechaMovimiento(LocalDate.now());
        inv.setProducto(new Producto());
        return inv;
    }

    private InventarioRequestDto requestValido(String tipo) {
        InventarioRequestDto r = new InventarioRequestDto();
        r.setCantidad(10);
        r.setTipoMovimiento(tipo);
        r.setFechaMovimiento(LocalDate.now());
        r.setProductoId(1L);
        return r;
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar movimientos retorna todos los registros como DTOs")
    void listarMovimientos_conDatos_retornaLista() {
        when(inventarioRepository.findAll()).thenReturn(List.of(movimientoValido("Entrada")));
        when(dtoMapper.toInventarioDtos(any())).thenReturn(List.of(new InventarioDto()));

        List<InventarioDto> result = inventarioService.findAll();

        assertThat(result).hasSize(1);
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar movimiento existente retorna el DTO")
    void buscarPorId_movimientoExistente_retornaDto() {
        Inventario inv = movimientoValido("Salida");
        InventarioDto dto = new InventarioDto();
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(dtoMapper.toDto(inv)).thenReturn(dto);

        InventarioDto result = inventarioService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar movimiento inexistente retorna null")
    void buscarPorId_movimientoInexistente_retornaNull() {
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThat(inventarioService.findById(99L)).isNull();
    }

    @ParameterizedTest(name = "id={0}")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Buscar por ids límite retorna null")
    void buscarPorId_idsLimite_retornaNull(long id) {
        when(inventarioRepository.findById(id)).thenReturn(Optional.empty());

        InventarioDto result = inventarioService.findById(id);

        assertThat(result).isNull();
    }

    // --- save (válidos) ---

    @Test
    @DisplayName("Registrar movimiento de Entrada válido guarda correctamente y retorna DTO")
    void registrarEntrada_cantidadValida_guardaMovimiento() {
        Inventario inv = movimientoValido("Entrada");
        InventarioDto dto = new InventarioDto();
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);
        when(inventarioRepository.save(inv)).thenReturn(inv);
        when(dtoMapper.toDto(inv)).thenReturn(dto);

        InventarioDto result = inventarioService.save(requestValido("Entrada"));

        assertThat(result).isEqualTo(dto);
        verify(inventarioRepository).save(inv);
    }

    @Test
    @DisplayName("Registrar movimiento de Salida válido guarda correctamente y retorna DTO")
    void registrarSalida_cantidadValida_guardaMovimiento() {
        Inventario inv = movimientoValido("Salida");
        InventarioDto dto = new InventarioDto();
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);
        when(inventarioRepository.save(inv)).thenReturn(inv);
        when(dtoMapper.toDto(inv)).thenReturn(dto);

        InventarioDto result = inventarioService.save(requestValido("Salida"));

        assertThat(result).isEqualTo(dto);
        verify(inventarioRepository).save(inv);
    }

    // --- save (inválidos) → verify(never()) ---

    @ParameterizedTest(name = "tipo inválido: \"{0}\"")
    @ValueSource(strings = {"entrada", "", "NINGUNO", "salida", "ENTRADA", "SALIDA"})
    @DisplayName("Tipos de movimiento inválidos lanzan excepción y no llaman save")
    void tiposMovimientoInvalidos_lanzanExcepcion(String tipo) {
        Inventario inv = movimientoValido(tipo);
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);

        InventarioRequestDto request = requestValido(tipo);
        assertThatThrownBy(() -> inventarioService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entrada");

        verify(inventarioRepository, never()).save(any());
    }

    @ParameterizedTest(name = "tipo nulo lanza excepción")
    @NullSource
    @DisplayName("Tipo de movimiento nulo lanza excepción y no llama save")
    void registrarMovimiento_tipoNulo_lanzaExcepcion(String tipo) {
        Inventario inv = movimientoValido("Entrada");
        inv.setTipoMovimiento(tipo);
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);

        InventarioRequestDto request = requestValido("Entrada");
        assertThatThrownBy(() -> inventarioService.save(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(inventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cantidad cero lanza excepción y no llama save")
    void registrarMovimiento_cantidadCero_lanzaExcepcion() {
        Inventario inv = movimientoValido("Entrada");
        inv.setCantidad(0);
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);

        InventarioRequestDto request = requestValido("Entrada");
        assertThatThrownBy(() -> inventarioService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad");

        verify(inventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cantidad negativa lanza excepción y no llama save")
    void registrarMovimiento_cantidadNegativa_lanzaExcepcion() {
        Inventario inv = movimientoValido("Entrada");
        inv.setCantidad(-5);
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);

        InventarioRequestDto request = requestValido("Entrada");
        assertThatThrownBy(() -> inventarioService.save(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(inventarioRepository, never()).save(any());
    }

    // --- findByProductoId ---

    @Test
    @DisplayName("Buscar por producto retorna sus movimientos como DTOs")
    void buscarPorProducto_retornaMovimientosDelProducto() {
        when(inventarioRepository.findByProductoId(3L))
                .thenReturn(List.of(movimientoValido("Entrada"), movimientoValido("Salida")));
        when(dtoMapper.toInventarioDtos(any())).thenReturn(List.of(new InventarioDto(), new InventarioDto()));

        List<InventarioDto> result = inventarioService.findByProductoId(3L);

        assertThat(result).hasSize(2);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(inventarioRepository.findAll()).thenThrow(new RuntimeException("Tiempo de espera agotado"));

        assertThatThrownBy(() -> inventarioService.findAll())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("DataAccessException del repositorio se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Inventario inv = movimientoValido("Entrada");
        when(dtoMapper.toEntity(any(InventarioRequestDto.class))).thenReturn(inv);
        when(inventarioRepository.save(inv))
                .thenThrow(new DataIntegrityViolationException("FK inválida"));

        InventarioRequestDto request = requestValido("Entrada");
        assertThatThrownBy(() -> inventarioService.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
