package com.minimarket.service;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.request.CarritoRequestDto;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.service.impl.CarritoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de CarritoService")
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Carrito carritoValido() {
        Carrito c = new Carrito();
        c.setId(1L);
        c.setUsuario(new Usuario());
        c.setProducto(new Producto());
        c.setCantidad(2);
        return c;
    }

    private CarritoRequestDto requestValido() {
        CarritoRequestDto r = new CarritoRequestDto();
        r.setUsuarioId(1L);
        r.setProductoId(1L);
        r.setCantidad(2);
        return r;
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar carrito retorna todos los ítems como DTOs")
    void listarCarrito_retornaListaCompleta() {
        when(carritoRepository.findAll()).thenReturn(List.of(carritoValido(), carritoValido()));
        when(dtoMapper.toCarritoDtos(any())).thenReturn(List.of(new CarritoDto(), new CarritoDto()));

        List<CarritoDto> result = carritoService.findAll();

        assertThat(result).hasSize(2);
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar ítem existente retorna el DTO")
    void buscarPorId_carritoExistente_retornaDto() {
        Carrito carrito = carritoValido();
        CarritoDto dto = new CarritoDto();
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(dtoMapper.toDto(carrito)).thenReturn(dto);

        CarritoDto result = carritoService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar ítem inexistente retorna null")
    void buscarPorId_carritoInexistente_retornaNull() {
        when(carritoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThat(carritoService.findById(99L)).isNull();
    }

    // --- save (válido) ---

    @Test
    @DisplayName("Agregar al carrito con datos válidos guarda correctamente y retorna DTO")
    void agregarAlCarrito_datosValidos_guardaCorrectamente() {
        Carrito carrito = carritoValido();
        CarritoDto dto = new CarritoDto();
        when(dtoMapper.toEntity(any(CarritoRequestDto.class))).thenReturn(carrito);
        when(carritoRepository.save(carrito)).thenReturn(carrito);
        when(dtoMapper.toDto(carrito)).thenReturn(dto);

        CarritoDto result = carritoService.save(requestValido());

        assertThat(result).isEqualTo(dto);
        verify(carritoRepository).save(carrito);
    }

    // --- save (inválidos) → verify(never()) ---

    @Test
    @DisplayName("Agregar request nulo lanza excepción y no llama save")
    void agregarAlCarrito_requestNulo_lanzaExcepcion() {
        assertThatThrownBy(() -> carritoService.save(null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Agregar al carrito sin usuario lanza excepción y no llama save")
    void agregarAlCarrito_usuarioNulo_lanzaExcepcion() {
        Carrito carritoSinUsuario = carritoValido();
        carritoSinUsuario.setUsuario(null);
        when(dtoMapper.toEntity(any(CarritoRequestDto.class))).thenReturn(carritoSinUsuario);

        CarritoRequestDto request = requestValido();
        assertThatThrownBy(() -> carritoService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usuario");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Agregar al carrito sin producto lanza excepción y no llama save")
    void agregarAlCarrito_productoNulo_lanzaExcepcion() {
        Carrito carritoSinProducto = carritoValido();
        carritoSinProducto.setProducto(null);
        when(dtoMapper.toEntity(any(CarritoRequestDto.class))).thenReturn(carritoSinProducto);

        CarritoRequestDto request = requestValido();
        assertThatThrownBy(() -> carritoService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("producto");

        verify(carritoRepository, never()).save(any());
    }

    @ParameterizedTest(name = "cantidad={0}")
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Cantidades inválidas (cero o negativas) lanzan excepción y no llaman save")
    void cantidadesInvalidas_lanzanExcepcion(int cantidad) {
        Carrito carritoConCantidadInvalida = carritoValido();
        carritoConCantidadInvalida.setCantidad(cantidad);
        when(dtoMapper.toEntity(any(CarritoRequestDto.class))).thenReturn(carritoConCantidadInvalida);

        CarritoRequestDto request = requestValido();
        assertThatThrownBy(() -> carritoService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad");

        verify(carritoRepository, never()).save(any());
    }

    // --- findByUsuarioId ---

    @Test
    @DisplayName("Buscar por usuario retorna sus ítems del carrito como DTOs")
    void buscarPorUsuario_retornaItemsDelUsuario() {
        when(carritoRepository.findByUsuarioId(2L)).thenReturn(List.of(carritoValido()));
        when(dtoMapper.toCarritoDtos(any())).thenReturn(List.of(new CarritoDto()));

        List<CarritoDto> result = carritoService.findByUsuarioId(2L);

        assertThat(result).hasSize(1);
        verify(carritoRepository).findByUsuarioId(2L);
    }

    // --- deleteById ---

    @Test
    @DisplayName("Eliminar ítem delega al repositorio")
    void eliminar_delegaAlRepositorio() {
        doNothing().when(carritoRepository).deleteById(1L);

        carritoService.deleteById(1L);

        verify(carritoRepository).deleteById(1L);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(carritoRepository.findAll()).thenThrow(new RuntimeException("Servidor caído"));

        assertThatThrownBy(() -> carritoService.findAll())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("DataAccessException se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Carrito carrito = carritoValido();
        when(dtoMapper.toEntity(any(CarritoRequestDto.class))).thenReturn(carrito);
        when(carritoRepository.save(carrito))
                .thenThrow(new DataIntegrityViolationException("Restricción violada"));

        CarritoRequestDto request = requestValido();
        assertThatThrownBy(() -> carritoService.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
