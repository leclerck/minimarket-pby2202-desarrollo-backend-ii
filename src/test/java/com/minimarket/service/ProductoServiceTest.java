package com.minimarket.service;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.ProductoDto;
import com.minimarket.dto.request.ProductoRequestDto;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    // --- Auxiliares ---

    private Producto productoValido() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Cereal");
        p.setPrecio(new BigDecimal("1200"));
        p.setStock(50);
        p.setCategoria(new Categoria());
        return p;
    }

    private ProductoRequestDto requestValido() {
        ProductoRequestDto r = new ProductoRequestDto();
        r.setNombre("Cereal");
        r.setPrecio(new BigDecimal("1200"));
        r.setStock(50);
        return r;
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar productos: repositorio vacío retorna lista vacía")
    void listarProductos_repositorioVacio_retornaListaVacia() {
        when(productoRepository.findAll()).thenReturn(List.of());
        when(dtoMapper.toProductoDtos(any())).thenReturn(List.of());

        List<ProductoDto> result = productoService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Listar productos: retorna todos los registros")
    void listarProductos_conDatos_retornaListaCompleta() {
        when(productoRepository.findAll()).thenReturn(List.of(productoValido(), productoValido()));
        when(dtoMapper.toProductoDtos(any())).thenReturn(List.of(new ProductoDto(), new ProductoDto()));

        List<ProductoDto> result = productoService.findAll();

        assertThat(result).hasSize(2);
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar por id: producto existente retorna DTO")
    void buscarPorId_productoExistente_retornaDto() {
        Producto producto = productoValido();
        ProductoDto dto = new ProductoDto();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(dtoMapper.toDto(producto)).thenReturn(dto);

        ProductoDto result = productoService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar por id: producto inexistente retorna null")
    void buscarPorId_productoInexistente_retornaNull() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        ProductoDto result = productoService.findById(99L);

        assertThat(result).isNull();
    }

    @ParameterizedTest(name = "id={0}")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Buscar por ids límite retorna null")
    void buscarPorId_idsLimite_retornaNull(long id) {
        when(productoRepository.findById(id)).thenReturn(Optional.empty());

        ProductoDto result = productoService.findById(id);

        assertThat(result).isNull();
    }

    // --- save (casos válidos) ---

    @Test
    @DisplayName("Guardar producto válido delega al repositorio y retorna DTO")
    void guardarProducto_valido_delegaAlRepositorioYRetornaDto() {
        Producto producto = productoValido();
        ProductoDto dto = new ProductoDto();
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);
        when(dtoMapper.toDto(producto)).thenReturn(dto);

        ProductoDto result = productoService.save(requestValido());

        assertThat(result).isEqualTo(dto);
        verify(productoRepository).save(producto);
    }

    // --- save (casos inválidos) → verify(never()) ---

    @Test
    @DisplayName("Guardar request nulo lanza IllegalArgumentException y no llama save")
    void guardarProducto_requestNulo_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> productoService.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guardar producto con nombre nulo lanza excepción y no llama save")
    void guardarProducto_nombreNulo_lanzaExcepcion() {
        Producto productoSinNombre = productoValido();
        productoSinNombre.setNombre(null);
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(productoSinNombre);

        ProductoRequestDto req1 = requestValido();
        assertThatThrownBy(() -> productoService.save(req1))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guardar producto con nombre vacío lanza excepción y no llama save")
    void guardarProducto_nombreVacio_lanzaExcepcion() {
        Producto productoNombreVacio = productoValido();
        productoNombreVacio.setNombre("   ");
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(productoNombreVacio);

        ProductoRequestDto req2 = requestValido();
        assertThatThrownBy(() -> productoService.save(req2))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guardar producto con precio en cero lanza excepción y no llama save")
    void guardarProducto_precioEnCero_lanzaExcepcion() {
        Producto productoPrecioCero = productoValido();
        productoPrecioCero.setPrecio(BigDecimal.ZERO);
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(productoPrecioCero);

        ProductoRequestDto req3 = requestValido();
        assertThatThrownBy(() -> productoService.save(req3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precio");

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guardar producto con precio negativo lanza excepción y no llama save")
    void guardarProducto_precioNegativo_lanzaExcepcion() {
        Producto productoPrecioNegativo = productoValido();
        productoPrecioNegativo.setPrecio(new BigDecimal("-10"));
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(productoPrecioNegativo);

        ProductoRequestDto req4 = requestValido();
        assertThatThrownBy(() -> productoService.save(req4))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guardar producto con stock negativo lanza excepción y no llama save")
    void guardarProducto_stockNegativo_lanzaExcepcion() {
        Producto productoStockNegativo = productoValido();
        productoStockNegativo.setStock(-1);
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(productoStockNegativo);

        ProductoRequestDto req5 = requestValido();
        assertThatThrownBy(() -> productoService.save(req5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");

        verify(productoRepository, never()).save(any());
    }

    // --- deleteById ---

    @Test
    @DisplayName("Eliminar producto existente llama a deleteById en el repositorio")
    void eliminarProducto_existente_llamaDeleteById() {
        doNothing().when(productoRepository).deleteById(1L);

        productoService.deleteById(1L);

        verify(productoRepository).deleteById(1L);
    }

    // --- findByCategoriaId ---

    @Test
    @DisplayName("Buscar por categoría retorna DTOs filtrados")
    void buscarPorCategoria_retornaProductosFiltrados() {
        when(productoRepository.findByCategoriaId(2L)).thenReturn(List.of(productoValido()));
        when(dtoMapper.toProductoDtos(any())).thenReturn(List.of(new ProductoDto()));

        List<ProductoDto> result = productoService.findByCategoriaId(2L);

        assertThat(result).hasSize(1);
        verify(productoRepository).findByCategoriaId(2L);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("Fallo con RuntimeException se propaga desde findAll")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(productoRepository.findAll()).thenThrow(new RuntimeException("Conexión fallida"));

        assertThatThrownBy(() -> productoService.findAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conexión fallida");
    }

    @Test
    @DisplayName("Fallo con DataAccessException se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Producto producto = productoValido();
        when(dtoMapper.toEntity(any(ProductoRequestDto.class))).thenReturn(producto);
        when(productoRepository.save(producto))
                .thenThrow(new DataIntegrityViolationException("Violación de integridad"));

        ProductoRequestDto req6 = requestValido();
        assertThatThrownBy(() -> productoService.save(req6))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
