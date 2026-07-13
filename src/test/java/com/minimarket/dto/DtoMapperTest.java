package com.minimarket.dto;

import com.minimarket.dto.request.*;
import com.minimarket.entity.*;
import com.minimarket.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de DtoMapper")
class DtoMapperTest {

    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private RolRepository rolRepository;

    @InjectMocks
    private DtoMapper dtoMapper;

    // ─────────────────────────── ROL ───────────────────────────

    @Nested
    @DisplayName("Rol")
    class RolTests {

        @Test
        @DisplayName("toDto mapea id y nombre correctamente")
        void toDto_rol_mapeaCamposCorrectamente() {
            Rol rol = new Rol(); rol.setId(1L); rol.setNombre("ADMIN");

            RolDto dto = dtoMapper.toDto(rol);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getNombre()).isEqualTo("ADMIN");
        }
    }

    // ─────────────────────────── CATEGORIA ───────────────────────────

    @Nested
    @DisplayName("Categoria")
    class CategoriaTests {

        @Test
        @DisplayName("toDto mapea id y nombre correctamente")
        void toDto_categoria_mapeaCamposCorrectamente() {
            Categoria categoria = new Categoria(); categoria.setId(1L); categoria.setNombre("Lácteos");

            CategoriaDto dto = dtoMapper.toDto(categoria);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getNombre()).isEqualTo("Lácteos");
        }

        @Test
        @DisplayName("toCategoriaDtos convierte lista de entidades a lista de DTOs")
        void toCategoriaDtos_listaDeEntidades_retornaListaDeDtos() {
            Categoria c1 = new Categoria(); c1.setId(1L); c1.setNombre("Bebidas");
            Categoria c2 = new Categoria(); c2.setId(2L); c2.setNombre("Snacks");

            List<CategoriaDto> dtos = dtoMapper.toCategoriaDtos(List.of(c1, c2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getNombre()).isEqualTo("Bebidas");
            assertThat(dtos.get(1).getNombre()).isEqualTo("Snacks");
        }

        @Test
        @DisplayName("toEntity crea entidad con nombre correcto")
        void toEntity_categoriaRequest_creaEntidadConNombre() {
            CategoriaRequestDto request = new CategoriaRequestDto();
            request.setNombre("Panadería");

            Categoria categoria = dtoMapper.toEntity(request);

            assertThat(categoria.getNombre()).isEqualTo("Panadería");
        }

        @Test
        @DisplayName("applyCategoriaRequest actualiza nombre de la entidad existente")
        void applyCategoriaRequest_actualizaNombreEnEntidadExistente() {
            Categoria categoria = new Categoria(); categoria.setNombre("Viejo");
            CategoriaRequestDto request = new CategoriaRequestDto();
            request.setNombre("Nuevo");

            dtoMapper.applyCategoriaRequest(categoria, request);

            assertThat(categoria.getNombre()).isEqualTo("Nuevo");
        }
    }

    // ─────────────────────────── PRODUCTO ───────────────────────────

    @Nested
    @DisplayName("Producto")
    class ProductoTests {

        @Test
        @DisplayName("toDto con categoría mapea todos los campos")
        void toDto_productoConCategoria_mapeaTodosCampos() {
            Categoria categoria = new Categoria(); categoria.setId(2L); categoria.setNombre("Bebidas");
            Producto producto = new Producto(); producto.setId(1L); producto.setNombre("Leche");
            producto.setPrecio(new BigDecimal("1.50")); producto.setStock(100); producto.setCategoria(categoria);

            ProductoDto dto = dtoMapper.toDto(producto);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getNombre()).isEqualTo("Leche");
            assertThat(dto.getPrecio()).isEqualByComparingTo("1.50");
            assertThat(dto.getStock()).isEqualTo(100);
            assertThat(dto.getCategoriaId()).isEqualTo(2L);
            assertThat(dto.getCategoriaNombre()).isEqualTo("Bebidas");
        }

        @Test
        @DisplayName("toDto sin categoría no propaga campos de categoría")
        void toDto_productoSinCategoria_camposCategoriaSonNull() {
            Producto producto = new Producto(); producto.setId(1L); producto.setNombre("X");
            producto.setPrecio(BigDecimal.TEN); producto.setStock(5); producto.setCategoria(null);

            ProductoDto dto = dtoMapper.toDto(producto);

            assertThat(dto.getCategoriaId()).isNull();
            assertThat(dto.getCategoriaNombre()).isNull();
        }

        @Test
        @DisplayName("toProductoDtos convierte lista correctamente")
        void toProductoDtos_listaDeProductos_retornaListaDeDtos() {
            Producto p1 = new Producto(); p1.setId(1L); p1.setNombre("A"); p1.setPrecio(BigDecimal.ONE); p1.setStock(1);
            Producto p2 = new Producto(); p2.setId(2L); p2.setNombre("B"); p2.setPrecio(BigDecimal.TEN); p2.setStock(2);

            List<ProductoDto> dtos = dtoMapper.toProductoDtos(List.of(p1, p2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getNombre()).isEqualTo("A");
            assertThat(dtos.get(1).getNombre()).isEqualTo("B");
        }

        @Test
        @DisplayName("toEntity sin categoriaId crea producto sin categoría sin consultar repo")
        void toEntity_productoRequestSinCategoriaId_noBuscaEnRepo() {
            ProductoRequestDto request = new ProductoRequestDto();
            request.setNombre("Pan"); request.setPrecio(new BigDecimal("0.50")); request.setStock(200);

            Producto producto = dtoMapper.toEntity(request);

            assertThat(producto.getNombre()).isEqualTo("Pan");
            assertThat(producto.getCategoria()).isNull();
            verify(categoriaRepository, never()).findById(any());
        }

        @Test
        @DisplayName("applyProductoRequest con categoriaId válido asigna la categoría")
        void applyProductoRequest_conCategoriaId_asignaCategoria() {
            Categoria categoria = new Categoria(); categoria.setId(3L); categoria.setNombre("Carnes");
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
            Producto producto = new Producto();
            ProductoRequestDto request = new ProductoRequestDto();
            request.setNombre("Pollo"); request.setPrecio(new BigDecimal("5.00")); request.setStock(50); request.setCategoriaId(3L);

            dtoMapper.applyProductoRequest(producto, request);

            assertThat(producto.getCategoria()).isEqualTo(categoria);
        }

        @Test
        @DisplayName("applyProductoRequest con categoriaId inexistente lanza IllegalArgumentException")
        void applyProductoRequest_categoriaNoEncontrada_lanzaExcepcion() {
            when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
            Producto producto = new Producto();
            ProductoRequestDto request = new ProductoRequestDto();
            request.setNombre("X"); request.setPrecio(BigDecimal.ONE); request.setStock(1); request.setCategoriaId(99L);

            assertThatThrownBy(() -> dtoMapper.applyProductoRequest(producto, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────── CARRITO ───────────────────────────

    @Nested
    @DisplayName("Carrito")
    class CarritoTests {

        @Test
        @DisplayName("toDto con usuario y producto mapea todos los campos")
        void toDto_carritoConUsuarioYProducto_mapeaTodosCampos() {
            Usuario usuario = new Usuario(); usuario.setId(1L); usuario.setUsername("cajero");
            Producto producto = new Producto(); producto.setId(2L); producto.setNombre("Queso");
            producto.setPrecio(BigDecimal.TEN); producto.setStock(10);
            Carrito carrito = new Carrito(); carrito.setId(1L); carrito.setCantidad(3);
            carrito.setUsuario(usuario); carrito.setProducto(producto);

            CarritoDto dto = dtoMapper.toDto(carrito);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getCantidad()).isEqualTo(3);
            assertThat(dto.getUsuarioId()).isEqualTo(1L);
            assertThat(dto.getUsuarioUsername()).isEqualTo("cajero");
            assertThat(dto.getProductoId()).isEqualTo(2L);
            assertThat(dto.getProductoNombre()).isEqualTo("Queso");
        }

        @Test
        @DisplayName("toDto sin usuario ni producto deja esos campos como null")
        void toDto_carritoSinRelaciones_camposSonNull() {
            Carrito carrito = new Carrito(); carrito.setId(5L); carrito.setCantidad(1);

            CarritoDto dto = dtoMapper.toDto(carrito);

            assertThat(dto.getUsuarioId()).isNull();
            assertThat(dto.getProductoId()).isNull();
        }

        @Test
        @DisplayName("toCarritoDtos convierte lista correctamente")
        void toCarritoDtos_listaDeCarritos_retornaListaDeDtos() {
            Carrito c1 = new Carrito(); c1.setId(1L); c1.setCantidad(2);
            Carrito c2 = new Carrito(); c2.setId(2L); c2.setCantidad(4);

            List<CarritoDto> dtos = dtoMapper.toCarritoDtos(List.of(c1, c2));

            assertThat(dtos).hasSize(2);
        }

        @Test
        @DisplayName("applyCarritoRequest con usuario y producto válidos los asigna")
        void applyCarritoRequest_conRelacionesValidas_asignaEntidades() {
            Usuario usuario = new Usuario(); usuario.setId(1L);
            Producto producto = new Producto(); producto.setId(2L);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
            Carrito carrito = new Carrito();
            CarritoRequestDto request = new CarritoRequestDto();
            request.setUsuarioId(1L); request.setProductoId(2L); request.setCantidad(5);

            dtoMapper.applyCarritoRequest(carrito, request);

            assertThat(carrito.getUsuario()).isEqualTo(usuario);
            assertThat(carrito.getProducto()).isEqualTo(producto);
            assertThat(carrito.getCantidad()).isEqualTo(5);
        }

        @Test
        @DisplayName("applyCarritoRequest con usuarioId inexistente lanza IllegalArgumentException")
        void applyCarritoRequest_usuarioNoEncontrado_lanzaExcepcion() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
            Carrito carrito = new Carrito();
            CarritoRequestDto request = new CarritoRequestDto();
            request.setUsuarioId(99L); request.setCantidad(1);

            assertThatThrownBy(() -> dtoMapper.applyCarritoRequest(carrito, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("applyCarritoRequest con productoId inexistente lanza IllegalArgumentException")
        void applyCarritoRequest_productoNoEncontrado_lanzaExcepcion() {
            when(productoRepository.findById(88L)).thenReturn(Optional.empty());
            Carrito carrito = new Carrito();
            CarritoRequestDto request = new CarritoRequestDto();
            request.setProductoId(88L); request.setCantidad(1);

            assertThatThrownBy(() -> dtoMapper.applyCarritoRequest(carrito, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("88");
        }
    }

    // ─────────────────────────── VENTA ───────────────────────────

    @Nested
    @DisplayName("Venta")
    class VentaTests {

        @Test
        @DisplayName("toDto con usuario y detalles mapea todos los campos")
        void toDto_ventaConUsuarioYDetalles_mapeaTodosCampos() {
            Usuario usuario = new Usuario(); usuario.setId(1L); usuario.setUsername("cajero");
            DetalleVenta det = new DetalleVenta(); det.setId(1L); det.setCantidad(2); det.setPrecio(BigDecimal.TEN);
            Venta venta = new Venta(); venta.setId(10L); venta.setFecha(LocalDate.of(2025, 1, 1));
            venta.setUsuario(usuario); venta.setDetalles(List.of(det));

            VentaDto dto = dtoMapper.toDto(venta);

            assertThat(dto.getId()).isEqualTo(10L);
            assertThat(dto.getFecha()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(dto.getUsuarioId()).isEqualTo(1L);
            assertThat(dto.getUsuarioUsername()).isEqualTo("cajero");
            assertThat(dto.getDetalles()).hasSize(1);
        }

        @Test
        @DisplayName("toDto con detalles null retorna lista vacía")
        void toDto_ventaConDetallesNull_retornaListaVaciaEnDetalles() {
            Venta venta = new Venta(); venta.setId(1L); venta.setFecha(LocalDate.now());
            venta.setDetalles(null);

            VentaDto dto = dtoMapper.toDto(venta);

            assertThat(dto.getDetalles()).isEmpty();
        }

        @Test
        @DisplayName("toDto sin usuario deja campos de usuario como null")
        void toDto_ventaSinUsuario_camposUsuarioSonNull() {
            Venta venta = new Venta(); venta.setId(1L); venta.setFecha(LocalDate.now());
            venta.setDetalles(List.of());

            VentaDto dto = dtoMapper.toDto(venta);

            assertThat(dto.getUsuarioId()).isNull();
            assertThat(dto.getUsuarioUsername()).isNull();
        }

        @Test
        @DisplayName("toVentaDtos convierte lista correctamente")
        void toVentaDtos_listaDeVentas_retornaListaDeDtos() {
            Venta v1 = new Venta(); v1.setId(1L); v1.setFecha(LocalDate.now()); v1.setDetalles(List.of());
            Venta v2 = new Venta(); v2.setId(2L); v2.setFecha(LocalDate.now()); v2.setDetalles(List.of());

            List<VentaDto> dtos = dtoMapper.toVentaDtos(List.of(v1, v2));

            assertThat(dtos).hasSize(2);
        }

        @Test
        @DisplayName("applyVentaRequest con usuarioId válido asigna usuario y fecha")
        void applyVentaRequest_conUsuarioValido_asignaUsuarioYFecha() {
            Usuario usuario = new Usuario(); usuario.setId(1L);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            Venta venta = new Venta();
            VentaRequestDto request = new VentaRequestDto();
            request.setUsuarioId(1L); request.setFecha(LocalDate.of(2025, 6, 1));

            dtoMapper.applyVentaRequest(venta, request);

            assertThat(venta.getUsuario()).isEqualTo(usuario);
            assertThat(venta.getFecha()).isEqualTo(LocalDate.of(2025, 6, 1));
        }

        @Test
        @DisplayName("applyVentaRequest con usuarioId inexistente lanza IllegalArgumentException")
        void applyVentaRequest_usuarioNoEncontrado_lanzaExcepcion() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
            Venta venta = new Venta();
            VentaRequestDto request = new VentaRequestDto();
            request.setUsuarioId(99L);

            assertThatThrownBy(() -> dtoMapper.applyVentaRequest(venta, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────── DETALLE VENTA ───────────────────────────

    @Nested
    @DisplayName("DetalleVenta")
    class DetalleVentaTests {

        @Test
        @DisplayName("toDto con venta y producto mapea todos los campos")
        void toDto_detalleVentaConVentaYProducto_mapeaTodosCampos() {
            Venta venta = new Venta(); venta.setId(10L);
            Producto producto = new Producto(); producto.setId(2L); producto.setNombre("Arroz");
            producto.setPrecio(BigDecimal.ONE); producto.setStock(10);
            DetalleVenta detalle = new DetalleVenta(); detalle.setId(1L); detalle.setCantidad(3);
            detalle.setPrecio(new BigDecimal("2.50")); detalle.setVenta(venta); detalle.setProducto(producto);

            DetalleVentaDto dto = dtoMapper.toDto(detalle);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getCantidad()).isEqualTo(3);
            assertThat(dto.getPrecio()).isEqualByComparingTo("2.50");
            assertThat(dto.getVentaId()).isEqualTo(10L);
            assertThat(dto.getProductoId()).isEqualTo(2L);
            assertThat(dto.getProductoNombre()).isEqualTo("Arroz");
        }

        @Test
        @DisplayName("toDto sin venta ni producto deja esos campos como null")
        void toDto_detalleVentaSinRelaciones_camposSonNull() {
            DetalleVenta detalle = new DetalleVenta(); detalle.setId(1L); detalle.setCantidad(1);
            detalle.setPrecio(BigDecimal.ONE);

            DetalleVentaDto dto = dtoMapper.toDto(detalle);

            assertThat(dto.getVentaId()).isNull();
            assertThat(dto.getProductoId()).isNull();
        }

        @Test
        @DisplayName("toDetalleVentaDtos convierte lista correctamente")
        void toDetalleVentaDtos_listaDeDetalles_retornaListaDeDtos() {
            DetalleVenta d1 = new DetalleVenta(); d1.setId(1L); d1.setCantidad(1); d1.setPrecio(BigDecimal.ONE);
            DetalleVenta d2 = new DetalleVenta(); d2.setId(2L); d2.setCantidad(2); d2.setPrecio(BigDecimal.TEN);

            List<DetalleVentaDto> dtos = dtoMapper.toDetalleVentaDtos(List.of(d1, d2));

            assertThat(dtos).hasSize(2);
        }

        @Test
        @DisplayName("applyDetalleVentaRequest con venta y producto válidos los asigna")
        void applyDetalleVentaRequest_conRelacionesValidas_asignaEntidades() {
            Venta venta = new Venta(); venta.setId(1L);
            Producto producto = new Producto(); producto.setId(2L);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
            when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
            DetalleVenta detalle = new DetalleVenta();
            DetalleVentaRequestDto request = new DetalleVentaRequestDto();
            request.setVentaId(1L); request.setProductoId(2L); request.setCantidad(4); request.setPrecio(new BigDecimal("3.00"));

            dtoMapper.applyDetalleVentaRequest(detalle, request);

            assertThat(detalle.getVenta()).isEqualTo(venta);
            assertThat(detalle.getProducto()).isEqualTo(producto);
            assertThat(detalle.getCantidad()).isEqualTo(4);
            assertThat(detalle.getPrecio()).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("applyDetalleVentaRequest con ventaId inexistente lanza IllegalArgumentException")
        void applyDetalleVentaRequest_ventaNoEncontrada_lanzaExcepcion() {
            when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
            DetalleVenta detalle = new DetalleVenta();
            DetalleVentaRequestDto request = new DetalleVentaRequestDto();
            request.setVentaId(99L); request.setCantidad(1); request.setPrecio(BigDecimal.ONE);

            assertThatThrownBy(() -> dtoMapper.applyDetalleVentaRequest(detalle, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("applyDetalleVentaRequest con productoId inexistente lanza IllegalArgumentException")
        void applyDetalleVentaRequest_productoNoEncontrado_lanzaExcepcion() {
            when(productoRepository.findById(88L)).thenReturn(Optional.empty());
            DetalleVenta detalle = new DetalleVenta();
            DetalleVentaRequestDto request = new DetalleVentaRequestDto();
            request.setProductoId(88L); request.setCantidad(1); request.setPrecio(BigDecimal.ONE);

            assertThatThrownBy(() -> dtoMapper.applyDetalleVentaRequest(detalle, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("88");
        }
    }

    // ─────────────────────────── INVENTARIO ───────────────────────────

    @Nested
    @DisplayName("Inventario")
    class InventarioTests {

        @Test
        @DisplayName("toDto con producto mapea todos los campos")
        void toDto_inventarioConProducto_mapeaTodosCampos() {
            Producto producto = new Producto(); producto.setId(3L); producto.setNombre("Fideos");
            producto.setPrecio(BigDecimal.ONE); producto.setStock(50);
            Inventario inventario = new Inventario(); inventario.setId(1L); inventario.setCantidad(20);
            inventario.setTipoMovimiento("Entrada"); inventario.setFechaMovimiento(LocalDate.of(2025, 3, 15));
            inventario.setProducto(producto);

            InventarioDto dto = dtoMapper.toDto(inventario);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getCantidad()).isEqualTo(20);
            assertThat(dto.getTipoMovimiento()).isEqualTo("Entrada");
            assertThat(dto.getFechaMovimiento()).isEqualTo(LocalDate.of(2025, 3, 15));
            assertThat(dto.getProductoId()).isEqualTo(3L);
            assertThat(dto.getProductoNombre()).isEqualTo("Fideos");
        }

        @Test
        @DisplayName("toDto sin producto no propaga campos de producto")
        void toDto_inventarioSinProducto_camposProductoSonNull() {
            Inventario inventario = new Inventario(); inventario.setId(1L); inventario.setCantidad(10);
            inventario.setTipoMovimiento("Salida"); inventario.setFechaMovimiento(LocalDate.now());

            InventarioDto dto = dtoMapper.toDto(inventario);

            assertThat(dto.getProductoId()).isNull();
            assertThat(dto.getProductoNombre()).isNull();
        }

        @Test
        @DisplayName("toInventarioDtos convierte lista y preserva tipo de movimiento")
        void toInventarioDtos_listaDeMovimientos_retornaListaDeDtos() {
            Inventario i1 = new Inventario(); i1.setId(1L); i1.setCantidad(10);
            i1.setTipoMovimiento("Entrada"); i1.setFechaMovimiento(LocalDate.now());
            Inventario i2 = new Inventario(); i2.setId(2L); i2.setCantidad(5);
            i2.setTipoMovimiento("Salida"); i2.setFechaMovimiento(LocalDate.now());

            List<InventarioDto> dtos = dtoMapper.toInventarioDtos(List.of(i1, i2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getTipoMovimiento()).isEqualTo("Entrada");
            assertThat(dtos.get(1).getTipoMovimiento()).isEqualTo("Salida");
        }

        @Test
        @DisplayName("applyInventarioRequest con productoId válido asigna producto y campos")
        void applyInventarioRequest_conProductoValido_asignaProducto() {
            Producto producto = new Producto(); producto.setId(5L);
            when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
            Inventario inventario = new Inventario();
            InventarioRequestDto request = new InventarioRequestDto();
            request.setProductoId(5L); request.setCantidad(30);
            request.setTipoMovimiento("Entrada"); request.setFechaMovimiento(LocalDate.of(2025, 5, 1));

            dtoMapper.applyInventarioRequest(inventario, request);

            assertThat(inventario.getProducto()).isEqualTo(producto);
            assertThat(inventario.getCantidad()).isEqualTo(30);
            assertThat(inventario.getTipoMovimiento()).isEqualTo("Entrada");
        }

        @Test
        @DisplayName("applyInventarioRequest con productoId inexistente lanza IllegalArgumentException")
        void applyInventarioRequest_productoNoEncontrado_lanzaExcepcion() {
            when(productoRepository.findById(77L)).thenReturn(Optional.empty());
            Inventario inventario = new Inventario();
            InventarioRequestDto request = new InventarioRequestDto();
            request.setProductoId(77L); request.setCantidad(5);
            request.setTipoMovimiento("Salida"); request.setFechaMovimiento(LocalDate.now());

            assertThatThrownBy(() -> dtoMapper.applyInventarioRequest(inventario, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("77");
        }
    }

    // ─────────────────────────── USUARIO ───────────────────────────

    @Nested
    @DisplayName("Usuario")
    class UsuarioTests {

        @Test
        @DisplayName("toDto con roles mapea id, username y roles correctamente")
        void toDto_usuarioConRoles_mapeaTodosCampos() {
            Rol rol = new Rol(); rol.setId(1L); rol.setNombre("CAJERO");
            Usuario usuario = new Usuario(); usuario.setId(1L); usuario.setUsername("cajero1");
            usuario.setPassword("secret"); usuario.setRoles(Set.of(rol));

            UsuarioDto dto = dtoMapper.toDto(usuario);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getUsername()).isEqualTo("cajero1");
            assertThat(dto.getRoles()).hasSize(1);
            assertThat(dto.getRoles().iterator().next().getNombre()).isEqualTo("CAJERO");
        }

        @Test
        @DisplayName("toDto con roles null no lanza excepción y mantiene roles null")
        void toDto_usuarioSinRoles_rolesNullNoLanzaExcepcion() {
            Usuario usuario = new Usuario(); usuario.setId(2L); usuario.setUsername("admin");
            usuario.setPassword("pass"); usuario.setRoles(null);

            UsuarioDto dto = dtoMapper.toDto(usuario);

            assertThat(dto.getId()).isEqualTo(2L);
            assertThat(dto.getRoles()).isNull();
        }

        @Test
        @DisplayName("toUsuarioDtos convierte lista correctamente")
        void toUsuarioDtos_listaDeUsuarios_retornaListaDeDtos() {
            Usuario u1 = new Usuario(); u1.setId(1L); u1.setUsername("a"); u1.setPassword("p"); u1.setRoles(Set.of());
            Usuario u2 = new Usuario(); u2.setId(2L); u2.setUsername("b"); u2.setPassword("p"); u2.setRoles(Set.of());

            List<UsuarioDto> dtos = dtoMapper.toUsuarioDtos(List.of(u1, u2));

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).getUsername()).isEqualTo("a");
            assertThat(dtos.get(1).getUsername()).isEqualTo("b");
        }

        @Test
        @DisplayName("toEntity crea usuario con username y password")
        void toEntity_usuarioRequest_creaEntidadConCredenciales() {
            UsuarioRequestDto request = new UsuarioRequestDto();
            request.setUsername("nuevo"); request.setPassword("clave123");

            Usuario usuario = dtoMapper.toEntity(request);

            assertThat(usuario.getUsername()).isEqualTo("nuevo");
            assertThat(usuario.getPassword()).isEqualTo("clave123");
        }

        @Test
        @DisplayName("applyUsuarioRequest con rolIds válidos asigna los roles")
        void applyUsuarioRequest_conRolIdsValidos_asignaRoles() {
            Rol rol = new Rol(); rol.setId(1L); rol.setNombre("ADMIN");
            when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
            Usuario usuario = new Usuario();
            UsuarioRequestDto request = new UsuarioRequestDto();
            request.setUsername("admin"); request.setPassword("pass"); request.setRolIds(Set.of(1L));

            dtoMapper.applyUsuarioRequest(usuario, request);

            assertThat(usuario.getUsername()).isEqualTo("admin");
            assertThat(usuario.getRoles()).containsExactly(rol);
        }

        @Test
        @DisplayName("applyUsuarioRequest con rolId inexistente lanza IllegalArgumentException")
        void applyUsuarioRequest_rolNoEncontrado_lanzaExcepcion() {
            when(rolRepository.findById(99L)).thenReturn(Optional.empty());
            Usuario usuario = new Usuario();
            UsuarioRequestDto request = new UsuarioRequestDto();
            request.setUsername("x"); request.setRolIds(Set.of(99L));

            assertThatThrownBy(() -> dtoMapper.applyUsuarioRequest(usuario, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("applyUsuarioRequest sin rolIds no consulta el repositorio")
        void applyUsuarioRequest_sinRolIds_noConsultaRepo() {
            Usuario usuario = new Usuario();
            UsuarioRequestDto request = new UsuarioRequestDto();
            request.setUsername("sin_rol"); request.setRolIds(null);

            dtoMapper.applyUsuarioRequest(usuario, request);

            assertThat(usuario.getRoles()).isNull();
            verify(rolRepository, never()).findById(any());
        }

        @Test
        @DisplayName("applyUsuarioRequest con password null no sobreescribe la contraseña existente")
        void applyUsuarioRequest_conPasswordNull_noSobreescribePassword() {
            Usuario usuario = new Usuario(); usuario.setPassword("original");
            UsuarioRequestDto request = new UsuarioRequestDto();
            request.setUsername("user"); request.setPassword(null);

            dtoMapper.applyUsuarioRequest(usuario, request);

            assertThat(usuario.getPassword()).isEqualTo("original");
        }
    }
}
