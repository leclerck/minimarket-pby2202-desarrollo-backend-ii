package com.minimarket.service;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.request.DetalleVentaRequestDto;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.DetalleVentaServiceImpl;
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
@DisplayName("Pruebas unitarias de DetalleVentaService (incluye lógica de stock)")
class DetalleVentaServiceTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private DetalleVentaServiceImpl detalleVentaService;

    private Producto productoConStock(int stock) {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Arroz");
        p.setStock(stock);
        p.setPrecio(new BigDecimal("500"));
        return p;
    }

    private DetalleVenta detalleValido(Producto producto, int cantidad) {
        DetalleVenta d = new DetalleVenta();
        d.setId(1L);
        d.setProducto(producto);
        d.setCantidad(cantidad);
        d.setPrecio(new BigDecimal("500"));
        d.setVenta(new Venta());
        return d;
    }

    private DetalleVentaRequestDto requestValido() {
        DetalleVentaRequestDto r = new DetalleVentaRequestDto();
        r.setVentaId(1L);
        r.setProductoId(1L);
        r.setCantidad(3);
        r.setPrecio(new BigDecimal("500"));
        return r;
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar detalles retorna todos los registros como DTOs")
    void listarDetalles_retornaListaCompleta() {
        when(detalleVentaRepository.findAll()).thenReturn(List.of(new DetalleVenta()));
        when(dtoMapper.toDetalleVentaDtos(any())).thenReturn(List.of(new DetalleVentaDto()));

        List<DetalleVentaDto> result = detalleVentaService.findAll();

        assertThat(result).hasSize(1);
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar detalle existente retorna el DTO")
    void buscarPorId_existente_retornaDto() {
        DetalleVenta detalle = detalleValido(productoConStock(10), 3);
        DetalleVentaDto dto = new DetalleVentaDto();
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalle));
        when(dtoMapper.toDto(detalle)).thenReturn(dto);

        DetalleVentaDto result = detalleVentaService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar detalle inexistente retorna null")
    void buscarPorId_inexistente_retornaNull() {
        when(detalleVentaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThat(detalleVentaService.findById(99L)).isNull();
    }

    // --- save (stock suficiente) ---

    @Test
    @DisplayName("Guardar detalle con stock suficiente disminuye el stock del producto")
    void guardarDetalle_stockSuficiente_disminuyeStockYGuarda() {
        Producto producto = productoConStock(10);
        DetalleVenta detalle = detalleValido(producto, 3);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);
        when(productoRepository.save(producto)).thenReturn(producto);
        when(detalleVentaRepository.save(detalle)).thenReturn(detalle);
        when(dtoMapper.toDto(detalle)).thenReturn(new DetalleVentaDto());

        detalleVentaService.save(requestValido());

        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("Guardar detalle con stock suficiente llama save en productoRepository")
    void guardarDetalle_stockSuficiente_llamaProductoRepositorioSave() {
        Producto producto = productoConStock(10);
        DetalleVenta detalle = detalleValido(producto, 3);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);
        when(productoRepository.save(producto)).thenReturn(producto);
        when(detalleVentaRepository.save(detalle)).thenReturn(detalle);
        when(dtoMapper.toDto(detalle)).thenReturn(new DetalleVentaDto());

        detalleVentaService.save(requestValido());

        verify(productoRepository).save(producto);
        verify(detalleVentaRepository).save(detalle);
    }

    @Test
    @DisplayName("Guardar detalle con exactamente el stock disponible lo deja en cero")
    void guardarDetalle_stockExacto_quedaEnCero() {
        Producto producto = productoConStock(5);
        DetalleVenta detalle = detalleValido(producto, 5);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);
        when(productoRepository.save(producto)).thenReturn(producto);
        when(detalleVentaRepository.save(detalle)).thenReturn(detalle);
        when(dtoMapper.toDto(detalle)).thenReturn(new DetalleVentaDto());

        detalleVentaService.save(requestValido());

        assertThat(producto.getStock()).isZero();
    }

    // --- save (stock insuficiente) ---

    @Test
    @DisplayName("Stock insuficiente lanza IllegalStateException y no llama save en detalleRepo")
    void guardarDetalle_stockInsuficiente_lanzaIllegalStateException() {
        Producto producto = productoConStock(2);
        DetalleVenta detalle = detalleValido(producto, 5);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);

        DetalleVentaRequestDto request = requestValido();
        assertThatThrownBy(() -> detalleVentaService.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(detalleVentaRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }

    @ParameterizedTest(name = "cantidad={0} con stock=5")
    @ValueSource(ints = {6, 100, Integer.MAX_VALUE})
    @DisplayName("Cantidades mayores al stock disponible lanzan IllegalStateException")
    void cantidadesMayoresQueStock_lanzanIllegalStateException(int cantidad) {
        Producto producto = productoConStock(5);
        DetalleVenta detalle = detalleValido(producto, cantidad);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);

        DetalleVentaRequestDto request = requestValido();
        assertThatThrownBy(() -> detalleVentaService.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(detalleVentaRepository, never()).save(any());
    }

    // --- save (cantidad inválida) ---

    @Test
    @DisplayName("Cantidad cero lanza IllegalArgumentException y no llama save")
    void guardarDetalle_cantidadCero_lanzaIllegalArgumentException() {
        Producto producto = productoConStock(10);
        DetalleVenta detalle = detalleValido(producto, 0);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);

        DetalleVentaRequestDto request = requestValido();
        assertThatThrownBy(() -> detalleVentaService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad");

        verify(detalleVentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cantidad negativa lanza IllegalArgumentException y no llama save")
    void guardarDetalle_cantidadNegativa_lanzaIllegalArgumentException() {
        Producto producto = productoConStock(10);
        DetalleVenta detalle = detalleValido(producto, -3);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);

        DetalleVentaRequestDto request = requestValido();
        assertThatThrownBy(() -> detalleVentaService.save(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(detalleVentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Producto nulo en la entidad mapeada lanza IllegalArgumentException")
    void guardarDetalle_productoNulo_lanzaExcepcion() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(3);
        detalle.setProducto(null);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);

        DetalleVentaRequestDto request = requestValido();
        assertThatThrownBy(() -> detalleVentaService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("producto");

        verify(detalleVentaRepository, never()).save(any());
    }

    // --- findByVentaId ---

    @Test
    @DisplayName("Buscar por venta retorna los detalles asociados como DTOs")
    void buscarPorVenta_retornaDetallesDeLaVenta() {
        when(detalleVentaRepository.findByVentaId(1L))
                .thenReturn(List.of(new DetalleVenta(), new DetalleVenta()));
        when(dtoMapper.toDetalleVentaDtos(any())).thenReturn(List.of(new DetalleVentaDto(), new DetalleVentaDto()));

        List<DetalleVentaDto> result = detalleVentaService.findByVentaId(1L);

        assertThat(result).hasSize(2);
    }

    // --- deleteById ---

    @Test
    @DisplayName("Eliminar detalle delega al repositorio")
    void eliminar_delegaAlRepositorio() {
        doNothing().when(detalleVentaRepository).deleteById(1L);

        detalleVentaService.deleteById(1L);

        verify(detalleVentaRepository).deleteById(1L);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(detalleVentaRepository.findAll()).thenThrow(new RuntimeException("Error de BD"));

        assertThatThrownBy(() -> detalleVentaService.findAll())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("DataAccessException se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Producto producto = productoConStock(10);
        DetalleVenta detalle = detalleValido(producto, 3);
        when(dtoMapper.toEntity(any(DetalleVentaRequestDto.class))).thenReturn(detalle);
        when(productoRepository.save(any())).thenReturn(producto);
        when(detalleVentaRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("FK inválida"));

        DetalleVentaRequestDto request = requestValido();
        assertThatThrownBy(() -> detalleVentaService.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
