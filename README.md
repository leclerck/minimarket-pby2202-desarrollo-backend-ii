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
| `cliente` | `cliente123` | USER | Cliente del minimarket |
| `staff` | `staff123` | STAFF | Personal / empleado |
| `admin` | `admin123` | ADMIN | Administrador del sistema |

Los usuarios y roles se crean automáticamente al iniciar la aplicación (`DataInitializer`).

## Roles y permisos

| Recurso | GET (lectura) | POST / PUT / DELETE (escritura) |
|---------|---------------|----------------------------------|
| `/public/**`, `/api/auth/**` | Público | Público (solo login) |
| `/api/productos`, `/api/categorias` | Público (catálogo) | STAFF, ADMIN |
| `/api/carrito` | USER, STAFF, ADMIN | USER, STAFF, ADMIN |
| `/api/ventas`, `/api/detalle-ventas` | STAFF, ADMIN | USER (crear), STAFF, ADMIN |
| `/api/inventario` | STAFF, ADMIN | STAFF, ADMIN |
| `/api/usuarios` | ADMIN | ADMIN |

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
  "username": "nuevo_cliente",
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
- Staff accede a inventario → 200
- Cliente intenta crear producto → 403

```bash
mvn test -Dtest=SecurityIntegrationTest
```

## Notas

- La base de datos H2 es **en memoria**: los datos se pierden al reiniciar la aplicación (los usuarios de prueba se recrean automáticamente).
- Como mejora a futuro deberá usarse HTTPS, una clave JWT segura en variables de entorno y una base de datos persistente (PostgreSQL, MySQL, etc.).
