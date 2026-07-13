# Minimarket — Backend API

API REST para la gestión de un minimarket, desarrollada con **Spring Boot 3.4**, **Spring Security** y **JWT**. Incluye autenticación stateless, control de acceso por roles, capa de **DTOs** y persistencia con JPA/H2.

## Requisitos

- Java 17+
- Maven 3.8+

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación inicia en `http://localhost:8080`.

Para compilar y ejecutar pruebas:

```bash
mvn clean test
```

## Stack tecnológico

| Componente | Tecnología |
|------------|------------|
| Framework | Spring Boot 3.4.1 |
| Seguridad | Spring Security + JWT (jjwt 0.12) |
| Persistencia | Spring Data JPA + Hibernate |
| API | DTOs de request/response (sin exponer entidades JPA) |
| Base de datos | H2 (en memoria) |
| Contraseñas | BCrypt |

## Autenticación JWT

### Flujo

1. El cliente envía credenciales a `POST /api/auth/login`.
2. El servidor valida usuario/contraseña contra la base de datos (BCrypt).
3. Si son correctas, devuelve un token JWT firmado con HMAC-SHA256.
4. Las peticiones protegidas deben incluir el header:

   ```
   Authorization: Bearer <token>
   ```

5. `JwtAuthenticationFilter` valida el token en cada petición y establece el contexto de seguridad.

### Ejemplo de login

**Request:**

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### Usuarios de prueba

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `cajero` | `cajero123` | CAJERO | Cajero del minimarket |
| `admin` | `admin123` | ADMIN | Administrador del sistema |

Los usuarios y roles se crean automáticamente al iniciar la aplicación (`DataInitializer`).

## Roles y permisos

El sistema define únicamente dos roles: **CAJERO** y **ADMIN**.

| Recurso | GET (lectura) | POST (crear) | PUT / DELETE |
|---------|---------------|--------------|--------------|
| `/public/**`, `/api/auth/**` | Público | Público (solo login) | — |
| `/api/productos`, `/api/categorias` | Público (catálogo) | ADMIN | ADMIN |
| `/api/carrito` | CAJERO, ADMIN | CAJERO, ADMIN | CAJERO, ADMIN |
| `/api/ventas` | CAJERO, ADMIN | CAJERO | — |
| `/api/detalle-ventas` | CAJERO, ADMIN | CAJERO | CAJERO, ADMIN |
| `/api/inventario` | CAJERO, ADMIN | CAJERO, ADMIN | CAJERO, ADMIN |
| `/api/usuarios` | ADMIN | ADMIN | ADMIN |

Los permisos se aplican con `@PreAuthorize` en los controladores y reglas globales en `SecurityConfig`.

## Capa DTO

Los controladores **no exponen entidades JPA** directamente. Cada recurso usa:

- **DTO de respuesta** (`ProductoDto`, `UsuarioDto`, etc.) en GET y en el cuerpo de respuesta de POST/PUT.
- **DTO de request** (`ProductoRequestDto`, `UsuarioRequestDto`, etc.) en POST y PUT.

La conversión Entity ↔ DTO se centraliza en `DtoMapper`. Las relaciones se envían/reciben como **IDs** (por ejemplo `categoriaId`, `usuarioId`), evitando referencias circulares y datos internos (contraseñas, colecciones bidireccionales).

### Ejemplo — producto

**POST `/api/productos`** (requiere rol STAFF o ADMIN):

```json
{
  "nombre": "Leche",
  "precio": 1200,
  "stock": 50,
  "categoriaId": 1
}
```

**Respuesta:**

```json
{
  "id": 1,
  "nombre": "Leche",
  "precio": 1200,
  "stock": 50,
  "categoriaId": 1,
  "categoriaNombre": "Lácteos"
}
```

### DTOs por recurso

| Recurso | Response DTO | Request DTO |
|---------|--------------|-------------|
| Usuarios | `UsuarioDto` | `UsuarioRequestDto` |
| Productos | `ProductoDto` | `ProductoRequestDto` |
| Categorías | `CategoriaDto` | `CategoriaRequestDto` |
| Carrito | `CarritoDto` | `CarritoRequestDto` |
| Ventas | `VentaDto` | `VentaRequestDto` |
| Detalle ventas | `DetalleVentaDto` | `DetalleVentaRequestDto` |
| Inventario | `InventarioDto` | `InventarioRequestDto` |

### Ejemplo — crear usuario (ADMIN)

**POST `/api/usuarios`:**

```json
{
  "username": "nuevo_user",
  "password": "clave123",
  "rolIds": [1]
}
```

La respuesta (`UsuarioDto`) incluye `id`, `username` y `roles`, pero **nunca** la contraseña.

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/public/hola` | Endpoint público de prueba |
| POST | `/api/auth/login` | Autenticación y obtención de JWT |
| GET/POST/PUT/DELETE | `/api/usuarios` | Gestión de usuarios (ADMIN) |
| GET/POST/PUT/DELETE | `/api/productos` | Catálogo de productos |
| GET/POST/PUT/DELETE | `/api/categorias` | Categorías de productos |
| GET/POST/PUT/DELETE | `/api/carrito` | Carrito de compras |
| GET/POST | `/api/ventas` | Registro de ventas |
| GET/POST/PUT/DELETE | `/api/detalle-ventas` | Detalle de ventas |
| GET/POST/PUT/DELETE | `/api/inventario` | Movimientos de inventario |
| GET | `/actuator/health` | Estado de salud de la aplicación |

## Configuración

Propiedades relevantes en `application.properties`:

```properties
jwt.secret=<clave-secreta-minimo-256-bits>
jwt.expiration=86400000          # 24 horas en milisegundos
spring.h2.console.enabled=false  # Consola H2 deshabilitada por seguridad
management.endpoints.web.exposure.include=health
```

## Estructura del proyecto

```
src/main/java/com/minimarket/
├── config/              # Inicialización de datos (roles y usuarios)
├── controller/          # Controladores REST (retornan DTOs)
├── dto/                 # DTOs de request/response y DtoMapper
├── entity/              # Entidades JPA (solo capa de persistencia)
├── repository/          # Repositorios Spring Data
├── service/             # Lógica de negocio
└── security/
    ├── config/          # SecurityConfig, manejo de excepciones
    ├── filter/          # JwtAuthenticationFilter
    ├── model/           # LoginRequest, LoginResponse, CustomUserDetails
    ├── service/         # CustomUserDetailsService
    └── util/            # JwtUtil (generación y validación)
```

## Medidas de seguridad implementadas

- **JWT stateless**: sin sesiones en servidor; adecuado para APIs REST.
- **BCrypt**: hash de contraseñas al crear/actualizar usuarios.
- **Control por roles**: `@EnableMethodSecurity` + `@PreAuthorize`.
- **Capa DTO**: las entidades JPA no se serializan en la API; las contraseñas no aparecen en `UsuarioDto`.
- **Respuestas JSON en errores**: 401 (no autenticado), 403 (acceso denegado) y 400 (datos inválidos, p. ej. ID de relación inexistente).
- **H2 console deshabilitada** y actuator limitado a `/health`.

## Códigos de respuesta HTTP

| Código | Significado |
|--------|-------------|
| 200 | Operación exitosa |
| 400 | Datos inválidos (p. ej. `categoriaId` o `usuarioId` inexistente) |
| 401 | No autenticado (token ausente, inválido o credenciales incorrectas) |
| 403 | Autenticado pero sin permiso para el recurso |
| 404 | Recurso no encontrado |

## Pruebas de seguridad

El proyecto incluye pruebas de integración en `SecurityIntegrationTest`:

- Acceso sin token → 401
- Cajero accede a inventario → 200
- Cajero intenta crear producto (solo ADMIN) → 403
- Credenciales incorrectas → 401

```bash
mvn test -Dtest=SecurityIntegrationTest
```

---

## Pruebas unitarias y calidad

### Stack de pruebas

| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| JUnit 5 | via Spring Boot | Framework de pruebas |
| Mockito | via Spring Boot | Mocking de dependencias |
| AssertJ | via Spring Boot | Aserciones fluidas |
| Spring Security Test | via Spring Boot | `@WithMockUser`, `@WithAnonymousUser` |
| JaCoCo | 0.8.12 | Cobertura de código (≥ 80% líneas) |

### Ejecución

```bash
# Ejecutar todas las pruebas
mvn test

# Generar reporte de cobertura (target/site/jacoco/index.html)
mvn verify

# Ejecutar grupo específico
mvn test -Dtest=ProductoServiceTest
mvn test -Dtest="com.minimarket.service.*"
mvn test -Dtest="com.minimarket.controller.*"
```

### Cobertura mínima

JaCoCo aplica un mínimo de **80% de cobertura de líneas**. El build falla si no se alcanza.

### Estructura de pruebas

```
src/test/java/com/minimarket/
├── UsuarioTest.java                          Entidad + CustomUserDetailsService + authorities
├── SecurityIntegrationTest.java              Integración JWT real (cajero/admin seed)
├── dto/
│   └── DtoMapperTest.java                    Mapeo Entity ↔ DTO + lookups de repositorios
├── service/
│   ├── ProductoServiceTest.java
│   ├── InventarioServiceTest.java
│   ├── VentaServiceTest.java
│   ├── UsuarioServiceTest.java
│   ├── CarritoServiceTest.java
│   ├── CategoriaServiceTest.java
│   ├── DetalleVentaServiceTest.java
│   └── RolServiceTest.java
└── controller/
    ├── ProductoControllerTest.java
    ├── InventarioControllerTest.java
    ├── VentaControllerTest.java
    ├── UsuarioControllerTest.java
    ├── CarritoControllerTest.java
    ├── CategoriaControllerTest.java
    └── DetalleVentaControllerTest.java
```

### Patrones de prueba

| Patrón | Uso |
|--------|-----|
| AAA (Arrange-Act-Assert) | Todos los tests de servicio y mapper |
| `@ExtendWith(MockitoExtension.class)` | Tests de capa de servicio y `DtoMapper` |
| `@SpringBootTest` + `@AutoConfigureMockMvc` | Tests de controladores |
| `@MockitoBean` | Aísla controladores del contexto real (Spring Boot 3.4+) |
| `@WithMockUser(roles = "ADMIN"\|"CAJERO")` | Simula usuario autenticado |
| `@WithAnonymousUser` | Simula acceso sin autenticar |
| `lenient().when(...)` | Stubs globales en `@BeforeEach` sin forzar uso en cada test |
| `when(...).thenReturn(null)` | Override por test para forzar rama "no encontrado" |
| `verify(never()).save(any())` | Confirma que el servicio no se llama cuando falla validación |
| `@ParameterizedTest` | Cubre múltiples entradas con un mismo test |
| `SecurityMockMvcRequestPostProcessors.user()` | Rol dinámico en `@ParameterizedTest` de controlador |
| `@Nested` | Agrupa tests de `DtoMapper` por entidad |

### Tabla de tests por método

#### ProductoService

| Test | Método | Descripción |
|------|--------|-------------|
| `listarProductos_repositorioVacio_retornaListaVacia` | `findAll()` | Repositorio vacío |
| `listarProductos_conDatos_retornaListaCompleta` | `findAll()` | Retorna todos |
| `buscarPorId_productoExistente_retornaProducto` | `findById()` | Producto encontrado |
| `buscarPorId_productoInexistente_retornaNull` | `findById()` | No encontrado |
| `buscarPorId_idsLimite_retornaNull` | `findById()` | IDs límite (0, -1, MAX) |
| `guardarProducto_valido_delegaAlRepositorio` | `save()` | Caso exitoso |
| `guardarProducto_productoNulo_lanzaIllegalArgumentException` | `save()` | Null guard |
| `guardarProducto_nombreNulo_lanzaExcepcion` | `save()` | Nombre null |
| `guardarProducto_nombreVacio_lanzaExcepcion` | `save()` | Nombre en blanco |
| `guardarProducto_precioEnCero_lanzaExcepcion` | `save()` | Precio ≤ 0 |
| `guardarProducto_precioNegativo_lanzaExcepcion` | `save()` | Precio negativo |
| `guardarProducto_stockNegativo_lanzaExcepcion` | `save()` | Stock < 0 |
| `eliminarProducto_existente_llamaDeleteById` | `deleteById()` | Delegación |
| `buscarPorCategoria_retornaProductosFiltrados` | `findByCategoriaId()` | Filtro por categoría |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |
| `repositorio_lanzaDataAccessException_propagaExcepcion` | `save()` | Fallo de BD |

#### InventarioService

| Test | Método | Descripción |
|------|--------|-------------|
| `registrarEntrada_cantidadValida_guardaMovimiento` | `save()` | Tipo "Entrada" |
| `registrarSalida_cantidadValida_guardaMovimiento` | `save()` | Tipo "Salida" |
| `tiposMovimientoInvalidos_lanzanExcepcion` | `save()` | Tipos incorrectos (6 casos) |
| `registrarMovimiento_tipoNulo_lanzaExcepcion` | `save()` | Tipo null |
| `registrarMovimiento_cantidadCero_lanzaExcepcion` | `save()` | Cantidad = 0 |
| `registrarMovimiento_cantidadNegativa_lanzaExcepcion` | `save()` | Cantidad < 0 |
| `buscarPorProducto_retornaMovimientosDelProducto` | `findByProductoId()` | Filtro |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |
| `repositorio_lanzaDataAccessException_propagaExcepcion` | `save()` | Fallo de BD |

#### VentaService

| Test | Método | Descripción |
|------|--------|-------------|
| `guardarVenta_conUsuarioValido_guardaCorrectamente` | `save()` | Caso exitoso |
| `guardarVenta_sinUsuario_lanzaExcepcion` | `save()` | Usuario null |
| `buscarPorUsuario_retornaVentasDelUsuario` | `findByUsuarioId()` | Filtro |
| `buscarPorId_ventaExistente_retornaVenta` | `findById()` | Encontrado |
| `buscarPorId_ventaInexistente_retornaNull` | `findById()` | No encontrado |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |
| `repositorio_lanzaDataAccessException_propagaExcepcion` | `save()` | Fallo de BD |

#### UsuarioService

| Test | Método | Descripción |
|------|--------|-------------|
| `guardarUsuario_contraseniaPlana_codificaConBCrypt` | `save()` | BCrypt aplicado |
| `guardarUsuario_contraseniaYaHasheada_noRecodifica` | `save()` | No re-hashea $2a$ |
| `guardarUsuario_contraseniaNula_guardaSinHashear` | `save()` | Password null |
| `guardarUsuario_usuarioNulo_lanzaExcepcion` | `save()` | Usuario null |
| `usernames_invalidos_lanzanExcepcion` | `save()` | null / "" / "  " |
| `buscarPorUsername_usuarioExistente_retornaOptional` | `findByUsername()` | Encontrado |
| `buscarPorUsername_usuarioInexistente_retornaOptionalVacio` | `findByUsername()` | No encontrado |
| `eliminarUsuario_delegaAlRepositorio` | `deleteById()` | Delegación |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |

#### CarritoService

| Test | Método | Descripción |
|------|--------|-------------|
| `agregarAlCarrito_datosValidos_guardaCorrectamente` | `save()` | Caso exitoso |
| `agregarAlCarrito_carritoNulo_lanzaExcepcion` | `save()` | Carrito null |
| `agregarAlCarrito_usuarioNulo_lanzaExcepcion` | `save()` | Usuario null |
| `agregarAlCarrito_productoNulo_lanzaExcepcion` | `save()` | Producto null |
| `cantidadesInvalidas_lanzanExcepcion` | `save()` | 0, -1, -100 |
| `buscarPorUsuario_retornaItemsDelUsuario` | `findByUsuarioId()` | Filtro |
| `eliminar_delegaAlRepositorio` | `deleteById()` | Delegación |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |

#### CategoriaService

| Test | Método | Descripción |
|------|--------|-------------|
| `guardarCategoria_nombreValido_guardaCorrectamente` | `save()` | Caso exitoso |
| `guardarCategoria_categoriaNula_lanzaExcepcion` | `save()` | Categoría null |
| `nombresInvalidos_lanzanExcepcion` | `save()` | null / "" / "  " |
| `eliminar_delegaAlRepositorio` | `deleteById()` | Delegación |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |

#### DetalleVentaService

| Test | Método | Descripción |
|------|--------|-------------|
| `guardarDetalle_stockSuficiente_disminuyeStockYGuarda` | `save()` | Stock reducido |
| `guardarDetalle_stockSuficiente_llamaProductoRepositorioSave` | `save()` | Repo producto llamado |
| `guardarDetalle_stockExacto_quedaEnCero` | `save()` | Stock exacto |
| `guardarDetalle_stockInsuficiente_lanzaIllegalStateException` | `save()` | Stock insuficiente |
| `cantidadesMayoresQueStock_lanzanIllegalStateException` | `save()` | 6, 100, MAX vs stock=5 |
| `guardarDetalle_cantidadCero_lanzaIllegalArgumentException` | `save()` | Cantidad = 0 |
| `guardarDetalle_cantidadNegativa_lanzaIllegalArgumentException` | `save()` | Cantidad negativa |
| `guardarDetalle_productoNulo_lanzaExcepcion` | `save()` | Producto null |
| `buscarPorVenta_retornaDetallesDeLaVenta` | `findByVentaId()` | Filtro |
| `eliminar_delegaAlRepositorio` | `deleteById()` | Delegación |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findAll()` | Fallo de BD |

#### RolService

| Test | Método | Descripción |
|------|--------|-------------|
| `buscarPorNombre_rolExistente_retornaOptionalConRol` | `findByNombre()` | Encontrado |
| `buscarPorNombre_rolInexistente_retornaOptionalVacio` | `findByNombre()` | No encontrado |
| `buscarPorNombre_nombreNulo_retornaOptionalVacio` | `findByNombre()` | Nombre null |
| `nombresDeRol_validos_consultanRepositorio` | `findByNombre()` | ADMIN, CAJERO |
| `repositorio_lanzaRuntimeException_propagaExcepcion` | `findByNombre()` | Fallo de BD |

#### UsuarioTest (entidad + autenticación)

| Test | Clase/Método | Descripción |
|------|-------------|-------------|
| `crearUsuario_conDatosValidos_asignaAtributosCorrectamente` | `Usuario` | Atributos correctos |
| `equals_usuariosConMismoId_sonIguales` | `Usuario.equals()` | Equals por id |
| `equals_usuariosConDistintoId_sonDiferentes` | `Usuario.equals()` | Distinto id |
| `agregarRoles_adminYCajero_asignaCorrectamente` | `Usuario.setRoles()` | Roles múltiples |
| `autenticar_usuarioExistente_retornaDetalles` | `loadUserByUsername()` | Auth exitosa |
| `autenticar_usuarioInexistente_lanzaUsernameNotFoundException` | `loadUserByUsername()` | Usuario no existe |
| `credencialesInvalidas_lanzanExcepcion` | `loadUserByUsername()` | "", "noexiste", inválidos |
| `roles_mapeadosConPrefixROLE_enAuthorities` | `CustomUserDetails.getAuthorities()` | ROLE_ prefix |
| `usuarioConMultiplesRoles_tieneTodasLasAuthorities` | `CustomUserDetails.getAuthorities()` | Múltiples roles |
| `usuarioSinRoles_tieneAuthoritiesVacias` | `CustomUserDetails.getAuthorities()` | Sin roles |
| `customUserDetails_flagsDeSeguridad_retornanTrue` | `CustomUserDetails` | isEnabled, etc. |

#### ProductoController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarProductos_sinAutenticacion_retorna200` | `GET /api/productos` | Catálogo público |
| `buscarProductoPorId_sinAutenticacion_retorna200` | `GET /api/productos/{id}` | Público |
| `crearProducto_conRolAdmin_retorna200` | `POST /api/productos` | ADMIN permitido |
| `crearProducto_conRolCajero_retorna403` | `POST /api/productos` | CAJERO denegado |
| `crearProducto_sinAutenticacion_retorna401` | `POST /api/productos` | No autenticado |
| `escritura_rolNoAdmin_retorna403` | `POST /api/productos` | CAJERO (parametrizado) |
| `actualizarProducto_conRolAdmin_productoInexistente_retorna404` | `PUT /api/productos/{id}` | No existe → 404 |
| `actualizarProducto_conRolCajero_retorna403` | `PUT /api/productos/{id}` | CAJERO denegado |
| `eliminarProducto_conRolAdmin_productoInexistente_retorna404` | `DELETE /api/productos/{id}` | No existe → 404 |
| `eliminarProducto_conRolCajero_retorna403` | `DELETE /api/productos/{id}` | CAJERO denegado |
| `eliminarProducto_conRolAdmin_retorna204` | `DELETE /api/productos/{id}` | ADMIN elimina → 204 |

#### CategoriaController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarCategorias_sinAutenticacion_retorna200` | `GET /api/categorias` | Catálogo público |
| `buscarCategoriaPorId_sinAutenticacion_retorna200` | `GET /api/categorias/{id}` | Público |
| `buscarCategoriaPorId_inexistente_retorna404` | `GET /api/categorias/{id}` | No existe → 404 |
| `guardarCategoria_conRolAdmin_retorna200` | `POST /api/categorias` | ADMIN permitido |
| `guardarCategoria_conRolCajero_retorna403` | `POST /api/categorias` | CAJERO denegado, sin llamar servicio |
| `guardarCategoria_sinAutenticacion_retorna401` | `POST /api/categorias` | No autenticado |
| `rolesNoPermitidosParaEscritura_retornan403` | `POST /api/categorias` | CAJERO (parametrizado) |
| `actualizarCategoria_conRolAdmin_categoriaInexistente_retorna404` | `PUT /api/categorias/{id}` | No existe → 404 |
| `actualizarCategoria_conRolCajero_retorna403` | `PUT /api/categorias/{id}` | CAJERO denegado |
| `eliminarCategoria_conRolAdmin_categoriaInexistente_retorna404` | `DELETE /api/categorias/{id}` | No existe → 404 |
| `eliminarCategoria_conRolCajero_retorna403` | `DELETE /api/categorias/{id}` | CAJERO denegado |
| `eliminarCategoria_conRolAdmin_retorna204` | `DELETE /api/categorias/{id}` | ADMIN elimina → 204 |

#### CarritoController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarCarrito_conRolCajero_retorna200` | `GET /api/carrito` | CAJERO permitido |
| `listarCarrito_conRolAdmin_retorna200` | `GET /api/carrito` | ADMIN permitido |
| `listarCarrito_sinAutenticacion_retorna401` | `GET /api/carrito` | No autenticado |
| `rolesAutorizados_puedenAcceder` | `GET /api/carrito` | CAJERO/ADMIN (parametrizado) |
| `obtenerCarritoPorId_inexistente_retorna404` | `GET /api/carrito/{id}` | No existe → 404 |
| `agregarAlCarrito_conRolCajero_retorna200` | `POST /api/carrito` | CAJERO permitido |
| `agregarAlCarrito_sinAutenticacion_retorna401` | `POST /api/carrito` | No autenticado, sin llamar servicio |
| `actualizarCarrito_conRolAdmin_itemInexistente_retorna404` | `PUT /api/carrito/{id}` | No existe → 404 |
| `eliminarDelCarrito_conRolAdmin_itemInexistente_retorna404` | `DELETE /api/carrito/{id}` | No existe → 404 |
| `eliminarDelCarrito_conRolAdmin_retorna204` | `DELETE /api/carrito/{id}` | ADMIN elimina → 204 |

#### InventarioController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarMovimientos_conRolCajero_retorna200` | `GET /api/inventario` | CAJERO permitido |
| `listarMovimientos_conRolAdmin_retorna200` | `GET /api/inventario` | ADMIN permitido |
| `listarMovimientos_sinAutenticacion_retorna401` | `GET /api/inventario` | No autenticado |
| `rolesAutorizados_retornan200` | `GET /api/inventario` | CAJERO/ADMIN (parametrizado) |
| `buscarMovimientoPorId_conRolCajero_retorna200` | `GET /api/inventario/{id}` | CAJERO permitido |
| `buscarMovimientoPorId_conRolAdmin_retorna200` | `GET /api/inventario/{id}` | ADMIN permitido |
| `buscarMovimientoPorId_sinAutenticacion_retorna401` | `GET /api/inventario/{id}` | No autenticado |
| `buscarMovimientoPorId_idsLimite_retorna404` | `GET /api/inventario/{id}` | 0, -1, MAX → 404 (parametrizado) |
| `registrarMovimiento_conRolCajero_retorna200` | `POST /api/inventario` | CAJERO permitido |
| `registrarMovimiento_conRolAdmin_retorna200` | `POST /api/inventario` | ADMIN permitido |
| `registrarMovimiento_sinAutenticacion_retorna401` | `POST /api/inventario` | No autenticado, sin llamar servicio |
| `actualizarMovimiento_conRolCajero_retorna200` | `PUT /api/inventario/{id}` | CAJERO permitido |
| `actualizarMovimiento_conRolAdmin_retorna200` | `PUT /api/inventario/{id}` | ADMIN permitido |
| `actualizarMovimiento_sinAutenticacion_retorna401` | `PUT /api/inventario/{id}` | No autenticado |
| `actualizarMovimientoPorId_idsLimite_retorna404` | `PUT /api/inventario/{id}` | 0, -1, MAX → `update()` vacío → 404 (parametrizado) |
| `eliminarMovimiento_conRolCajero_retorna204` | `DELETE /api/inventario/{id}` | CAJERO elimina → 204 |
| `eliminarMovimiento_conRolAdmin_retorna204` | `DELETE /api/inventario/{id}` | ADMIN elimina → 204 |
| `eliminarMovimiento_sinAutenticacion_retorna401` | `DELETE /api/inventario/{id}` | No autenticado |
| `eliminarMovimientoPorId_idsLimite_retorna404` | `DELETE /api/inventario/{id}` | 0, -1, MAX → 404 (parametrizado) |

#### UsuarioController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarUsuarios_conRolAdmin_retorna200` | `GET /api/usuarios` | ADMIN permitido |
| `listarUsuarios_conRolCajero_retorna403` | `GET /api/usuarios` | CAJERO denegado |
| `listarUsuarios_sinAutenticacion_retorna401` | `GET /api/usuarios` | No autenticado |
| `rolNoAdmin_retorna403` | `GET /api/usuarios` | CAJERO (parametrizado) |
| `buscarUsuarioPorId_conRolAdmin_retorna200` | `GET /api/usuarios/{id}` | ADMIN permitido |
| `buscarUsuarioPorId_conRolCajero_retorna403` | `GET /api/usuarios/{id}` | CAJERO denegado |
| `buscarUsuarioPorId_sinAutenticacion_retorna401` | `GET /api/usuarios/{id}` | No autenticado |
| `buscarUsuarioPorId_idsLimite_retorna404` | `GET /api/usuarios/{id}` | 0, -1, MAX → 404 (parametrizado, ADMIN) |
| `crearUsuario_conRolAdmin_retorna200` | `POST /api/usuarios` | ADMIN permitido |
| `crearUsuario_conRolCajero_retorna403` | `POST /api/usuarios` | CAJERO denegado, sin llamar servicio |
| `actualizarUsuario_conRolAdmin_retorna200` | `PUT /api/usuarios/{id}` | ADMIN permitido |
| `actualizarUsuario_conRolCajero_retorna403` | `PUT /api/usuarios/{id}` | CAJERO denegado |
| `actualizarUsuario_sinAutenticacion_retorna401` | `PUT /api/usuarios/{id}` | No autenticado |
| `eliminarUsuario_conRolAdmin_usuarioInexistente_retorna404` | `DELETE /api/usuarios/{id}` | No existe → 404 |
| `eliminarUsuario_conRolCajero_retorna403` | `DELETE /api/usuarios/{id}` | CAJERO denegado, sin llamar servicio |

#### VentaController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarVentas_conRolCajero_retorna200` | `GET /api/ventas` | CAJERO permitido |
| `listarVentas_conRolAdmin_retorna200` | `GET /api/ventas` | ADMIN permitido |
| `listarVentas_sinAutenticacion_retorna401` | `GET /api/ventas` | No autenticado |
| `crearVenta_conRolCajero_retorna200` | `POST /api/ventas` | CAJERO crea venta |
| `crearVenta_conRolAdmin_retorna403` | `POST /api/ventas` | ADMIN no puede crear ventas, sin llamar servicio |
| `crearVenta_sinAutenticacion_retorna401` | `POST /api/ventas` | No autenticado, sin llamar servicio |
| `obtenerVenta_inexistente_retorna404` | `GET /api/ventas/{id}` | No encontrado → 404 |

#### DetalleVentaController (autorización)

| Test | Endpoint | Descripción |
|------|----------|-------------|
| `listarDetalles_conRolCajero_retorna200` | `GET /api/detalle-ventas` | CAJERO permitido |
| `listarDetalles_conRolAdmin_retorna200` | `GET /api/detalle-ventas` | ADMIN permitido |
| `listarDetalles_sinAutenticacion_retorna401` | `GET /api/detalle-ventas` | No autenticado |
| `obtenerDetallePorId_conRolAdmin_retorna200` | `GET /api/detalle-ventas/{id}` | ADMIN permitido |
| `obtenerDetalle_inexistente_retorna404` | `GET /api/detalle-ventas/{id}` | No existe → 404 |
| `obtenerDetallePorId_sinAutenticacion_retorna401` | `GET /api/detalle-ventas/{id}` | No autenticado |
| `guardarDetalle_conRolCajero_retorna200` | `POST /api/detalle-ventas` | CAJERO crea detalle |
| `guardarDetalle_conRolAdmin_retorna403` | `POST /api/detalle-ventas` | Solo CAJERO puede crear, sin llamar servicio |
| `guardarDetalle_sinAutenticacion_retorna401` | `POST /api/detalle-ventas` | No autenticado, sin llamar servicio |
| `actualizarDetalle_conRolAdmin_detalleInexistente_retorna404` | `PUT /api/detalle-ventas/{id}` | No existe → 404 |
| `eliminarDetalle_conRolAdmin_detalleInexistente_retorna404` | `DELETE /api/detalle-ventas/{id}` | No existe → 404 |
| `eliminarDetalle_conRolAdmin_retorna204` | `DELETE /api/detalle-ventas/{id}` | ADMIN elimina → 204 |

#### DtoMapper (mapeo Entity ↔ DTO)

| Test | Método | Descripción |
|------|--------|-------------|
| `toDto_rol_mapeaCamposCorrectamente` | `toDto(Rol)` | id y nombre correctos |
| `toDto_categoria_mapeaCamposCorrectamente` | `toDto(Categoria)` | id y nombre correctos |
| `toCategoriaDtos_listaDeEntidades_retornaListaDeDtos` | `toCategoriaDtos()` | Colección de entidades |
| `toEntity_categoriaRequest_creaEntidadConNombre` | `toEntity(CategoriaRequestDto)` | Entidad sin id |
| `applyCategoriaRequest_actualizaNombreEnEntidadExistente` | `applyCategoriaRequest()` | Actualización in-place |
| `toDto_productoConCategoria_mapeaTodosCampos` | `toDto(Producto)` | Todos los campos con categoría |
| `toDto_productoSinCategoria_camposCategoriaSonNull` | `toDto(Producto)` | Categoría null → campos null |
| `toProductoDtos_listaDeProductos_retornaListaDeDtos` | `toProductoDtos()` | Colección de entidades |
| `toEntity_productoRequestSinCategoriaId_noBuscaEnRepo` | `toEntity(ProductoRequestDto)` | Sin categoriaId, repo no consultado |
| `applyProductoRequest_conCategoriaId_asignaCategoria` | `applyProductoRequest()` | Busca y asigna categoría |
| `applyProductoRequest_categoriaNoEncontrada_lanzaExcepcion` | `applyProductoRequest()` | categoriaId inválido → excepción |
| `toDto_carritoConUsuarioYProducto_mapeaTodosCampos` | `toDto(Carrito)` | Todos los campos |
| `toDto_carritoSinRelaciones_camposSonNull` | `toDto(Carrito)` | Relaciones null → campos null |
| `toCarritoDtos_listaDeCarritos_retornaListaDeDtos` | `toCarritoDtos()` | Colección de entidades |
| `applyCarritoRequest_conRelacionesValidas_asignaEntidades` | `applyCarritoRequest()` | Usuario y producto asignados |
| `applyCarritoRequest_usuarioNoEncontrado_lanzaExcepcion` | `applyCarritoRequest()` | usuarioId inválido → excepción |
| `applyCarritoRequest_productoNoEncontrado_lanzaExcepcion` | `applyCarritoRequest()` | productoId inválido → excepción |
| `toDto_ventaConUsuarioYDetalles_mapeaTodosCampos` | `toDto(Venta)` | Usuario e IDs de detalles |
| `toDto_ventaConDetallesNull_retornaListaVaciaEnDetalles` | `toDto(Venta)` | Detalles null → lista vacía |
| `toDto_ventaSinUsuario_camposUsuarioSonNull` | `toDto(Venta)` | Usuario null → campos null |
| `toVentaDtos_listaDeVentas_retornaListaDeDtos` | `toVentaDtos()` | Colección de entidades |
| `applyVentaRequest_conUsuarioValido_asignaUsuarioYFecha` | `applyVentaRequest()` | Usuario y fecha asignados |
| `applyVentaRequest_usuarioNoEncontrado_lanzaExcepcion` | `applyVentaRequest()` | usuarioId inválido → excepción |
| `toDto_detalleVentaConVentaYProducto_mapeaTodosCampos` | `toDto(DetalleVenta)` | Todos los campos |
| `toDto_detalleVentaSinRelaciones_camposSonNull` | `toDto(DetalleVenta)` | Relaciones null → campos null |
| `toDetalleVentaDtos_listaDeDetalles_retornaListaDeDtos` | `toDetalleVentaDtos()` | Colección de entidades |
| `applyDetalleVentaRequest_conRelacionesValidas_asignaEntidades` | `applyDetalleVentaRequest()` | Venta y producto asignados |
| `applyDetalleVentaRequest_ventaNoEncontrada_lanzaExcepcion` | `applyDetalleVentaRequest()` | ventaId inválido → excepción |
| `applyDetalleVentaRequest_productoNoEncontrado_lanzaExcepcion` | `applyDetalleVentaRequest()` | productoId inválido → excepción |
| `toDto_inventarioConProducto_mapeaTodosCampos` | `toDto(Inventario)` | Todos los campos con producto |
| `toDto_inventarioSinProducto_camposProductoSonNull` | `toDto(Inventario)` | Producto null → campos null |
| `toInventarioDtos_listaDeMovimientos_retornaListaDeDtos` | `toInventarioDtos()` | Colección de entidades |
| `applyInventarioRequest_conProductoValido_asignaProducto` | `applyInventarioRequest()` | Producto asignado |
| `applyInventarioRequest_productoNoEncontrado_lanzaExcepcion` | `applyInventarioRequest()` | productoId inválido → excepción |
| `toDto_usuarioConRoles_mapeaTodosCampos` | `toDto(Usuario)` | Username y lista de roles |
| `toDto_usuarioSinRoles_rolesNullNoLanzaExcepcion` | `toDto(Usuario)` | Roles null → lista vacía, sin excepción |
| `toUsuarioDtos_listaDeUsuarios_retornaListaDeDtos` | `toUsuarioDtos()` | Colección de entidades |
| `toEntity_usuarioRequest_creaEntidadConCredenciales` | `toEntity(UsuarioRequestDto)` | Username y password asignados |
| `applyUsuarioRequest_conRolIdsValidos_asignaRoles` | `applyUsuarioRequest()` | Roles asignados por id |
| `applyUsuarioRequest_rolNoEncontrado_lanzaExcepcion` | `applyUsuarioRequest()` | rolId inválido → excepción |
| `applyUsuarioRequest_sinRolIds_noConsultaRepo` | `applyUsuarioRequest()` | rolIds null → repositorio no consultado |
| `applyUsuarioRequest_conPasswordNull_noSobreescribePassword` | `applyUsuarioRequest()` | Password null no pisa el existente |

## Notas

- La base de datos H2 es **en memoria**: los datos se pierden al reiniciar la aplicación (los usuarios de prueba se recrean automáticamente).
- Como mejora a futuro deberá usarse HTTPS, una clave JWT segura en variables de entorno y una base de datos persistente (PostgreSQL, MySQL, etc.).
