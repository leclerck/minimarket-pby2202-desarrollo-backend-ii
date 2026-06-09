package com.minimarket.dto;

import com.minimarket.entity.*;
import com.minimarket.repository.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final RolRepository rolRepository;

    public DtoMapper(CategoriaRepository categoriaRepository,
                     UsuarioRepository usuarioRepository,
                     ProductoRepository productoRepository,
                     VentaRepository ventaRepository,
                     RolRepository rolRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.rolRepository = rolRepository;
    }

    // --- Rol ---

    public RolDto toDto(Rol rol) {
        RolDto dto = new RolDto();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        return dto;
    }

    // --- Usuario ---

    public UsuarioDto toDto(Usuario usuario) {
        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        if (usuario.getRoles() != null) {
            dto.setRoles(usuario.getRoles().stream().map(this::toDto).collect(Collectors.toSet()));
        }
        return dto;
    }

    public List<UsuarioDto> toUsuarioDtos(List<Usuario> usuarios) {
        return usuarios.stream().map(this::toDto).toList();
    }

    public Usuario toEntity(UsuarioRequestDto dto) {
        Usuario usuario = new Usuario();
        applyUsuarioRequest(usuario, dto);
        return usuario;
    }

    public void applyUsuarioRequest(Usuario usuario, UsuarioRequestDto dto) {
        usuario.setUsername(dto.getUsername());
        if (dto.getPassword() != null) {
            usuario.setPassword(dto.getPassword());
        }
        if (dto.getRolIds() != null) {
            Set<Rol> roles = dto.getRolIds().stream()
                    .map(id -> rolRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id)))
                    .collect(Collectors.toSet());
            usuario.setRoles(roles);
        }
    }

    // --- Categoria ---

    public CategoriaDto toDto(Categoria categoria) {
        CategoriaDto dto = new CategoriaDto();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        return dto;
    }

    public List<CategoriaDto> toCategoriaDtos(List<Categoria> categorias) {
        return categorias.stream().map(this::toDto).toList();
    }

    public Categoria toEntity(CategoriaRequestDto dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        return categoria;
    }

    public void applyCategoriaRequest(Categoria categoria, CategoriaRequestDto dto) {
        categoria.setNombre(dto.getNombre());
    }

    // --- Producto ---

    public ProductoDto toDto(Producto producto) {
        ProductoDto dto = new ProductoDto();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }
        return dto;
    }

    public List<ProductoDto> toProductoDtos(List<Producto> productos) {
        return productos.stream().map(this::toDto).toList();
    }

    public Producto toEntity(ProductoRequestDto dto) {
        Producto producto = new Producto();
        applyProductoRequest(producto, dto);
        return producto;
    }

    public void applyProductoRequest(Producto producto, ProductoRequestDto dto) {
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }
    }

    // --- Carrito ---

    public CarritoDto toDto(Carrito carrito) {
        CarritoDto dto = new CarritoDto();
        dto.setId(carrito.getId());
        dto.setCantidad(carrito.getCantidad());
        if (carrito.getUsuario() != null) {
            dto.setUsuarioId(carrito.getUsuario().getId());
            dto.setUsuarioUsername(carrito.getUsuario().getUsername());
        }
        if (carrito.getProducto() != null) {
            dto.setProductoId(carrito.getProducto().getId());
            dto.setProductoNombre(carrito.getProducto().getNombre());
        }
        return dto;
    }

    public List<CarritoDto> toCarritoDtos(List<Carrito> items) {
        return items.stream().map(this::toDto).toList();
    }

    public Carrito toEntity(CarritoRequestDto dto) {
        Carrito carrito = new Carrito();
        applyCarritoRequest(carrito, dto);
        return carrito;
    }

    public void applyCarritoRequest(Carrito carrito, CarritoRequestDto dto) {
        carrito.setCantidad(dto.getCantidad());
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getUsuarioId()));
            carrito.setUsuario(usuario);
        }
        if (dto.getProductoId() != null) {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + dto.getProductoId()));
            carrito.setProducto(producto);
        }
    }

    // --- Venta ---

    public VentaDto toDto(Venta venta) {
        VentaDto dto = new VentaDto();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        if (venta.getUsuario() != null) {
            dto.setUsuarioId(venta.getUsuario().getId());
            dto.setUsuarioUsername(venta.getUsuario().getUsername());
        }
        if (venta.getDetalles() != null) {
            dto.setDetalles(venta.getDetalles().stream().map(this::toDto).toList());
        } else {
            dto.setDetalles(Collections.emptyList());
        }
        return dto;
    }

    public List<VentaDto> toVentaDtos(List<Venta> ventas) {
        return ventas.stream().map(this::toDto).toList();
    }

    public Venta toEntity(VentaRequestDto dto) {
        Venta venta = new Venta();
        applyVentaRequest(venta, dto);
        return venta;
    }

    public void applyVentaRequest(Venta venta, VentaRequestDto dto) {
        venta.setFecha(dto.getFecha());
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getUsuarioId()));
            venta.setUsuario(usuario);
        }
    }

    // --- DetalleVenta ---

    public DetalleVentaDto toDto(DetalleVenta detalle) {
        DetalleVentaDto dto = new DetalleVentaDto();
        dto.setId(detalle.getId());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecio(detalle.getPrecio());
        if (detalle.getVenta() != null) {
            dto.setVentaId(detalle.getVenta().getId());
        }
        if (detalle.getProducto() != null) {
            dto.setProductoId(detalle.getProducto().getId());
            dto.setProductoNombre(detalle.getProducto().getNombre());
        }
        return dto;
    }

    public List<DetalleVentaDto> toDetalleVentaDtos(List<DetalleVenta> detalles) {
        return detalles.stream().map(this::toDto).toList();
    }

    public DetalleVenta toEntity(DetalleVentaRequestDto dto) {
        DetalleVenta detalle = new DetalleVenta();
        applyDetalleVentaRequest(detalle, dto);
        return detalle;
    }

    public void applyDetalleVentaRequest(DetalleVenta detalle, DetalleVentaRequestDto dto) {
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecio(dto.getPrecio());
        if (dto.getVentaId() != null) {
            Venta venta = ventaRepository.findById(dto.getVentaId())
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + dto.getVentaId()));
            detalle.setVenta(venta);
        }
        if (dto.getProductoId() != null) {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + dto.getProductoId()));
            detalle.setProducto(producto);
        }
    }

    // --- Inventario ---

    public InventarioDto toDto(Inventario inventario) {
        InventarioDto dto = new InventarioDto();
        dto.setId(inventario.getId());
        dto.setCantidad(inventario.getCantidad());
        dto.setTipoMovimiento(inventario.getTipoMovimiento());
        dto.setFechaMovimiento(inventario.getFechaMovimiento());
        if (inventario.getProducto() != null) {
            dto.setProductoId(inventario.getProducto().getId());
            dto.setProductoNombre(inventario.getProducto().getNombre());
        }
        return dto;
    }

    public List<InventarioDto> toInventarioDtos(List<Inventario> movimientos) {
        return movimientos.stream().map(this::toDto).toList();
    }

    public Inventario toEntity(InventarioRequestDto dto) {
        Inventario inventario = new Inventario();
        applyInventarioRequest(inventario, dto);
        return inventario;
    }

    public void applyInventarioRequest(Inventario inventario, InventarioRequestDto dto) {
        inventario.setCantidad(dto.getCantidad());
        inventario.setTipoMovimiento(dto.getTipoMovimiento());
        inventario.setFechaMovimiento(dto.getFechaMovimiento());
        if (dto.getProductoId() != null) {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + dto.getProductoId()));
            inventario.setProducto(producto);
        }
    }
}
