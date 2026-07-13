package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.model.CustomUserDetails;
import com.minimarket.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de entidad Usuario y autenticación")
class UsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // ==================== ENTIDAD ====================

    @Test
    @DisplayName("Crear usuario con datos válidos asigna atributos correctamente")
    void crearUsuario_conDatosValidos_asignaAtributosCorrectamente() {
        Set<Rol> roles = Set.of(new Rol("ADMIN"));
        Usuario usuario = new Usuario();
        usuario.setUsername("adminUser");
        usuario.setPassword("securePassword123");
        usuario.setRoles(roles);

        assertThat(usuario).isNotNull();
        assertThat(usuario.getUsername()).isEqualTo("adminUser");
        assertThat(usuario.getPassword()).isEqualTo("securePassword123");
        assertThat(usuario.getRoles()).hasSize(1);
        assertThat(usuario.getRoles()).anyMatch(r -> r.getNombre().equals("ADMIN"));
    }

    @Test
    @DisplayName("Equals compara solo por id: mismo id → iguales")
    void equals_usuariosConMismoId_sonIguales() {
        Usuario u1 = new Usuario();
        u1.setId(1L);
        u1.setUsername("cajero");

        Usuario u2 = new Usuario();
        u2.setId(1L);
        u2.setUsername("otro_nombre");

        assertThat(u1).isEqualTo(u2);
    }

    @Test
    @DisplayName("Equals compara solo por id: distinto id → distintos")
    void equals_usuariosConDistintoId_sonDiferentes() {
        Usuario u1 = new Usuario();
        u1.setId(1L);

        Usuario u2 = new Usuario();
        u2.setId(2L);

        assertThat(u1).isNotEqualTo(u2);
    }

    @Test
    @DisplayName("Agregar roles ADMIN y CAJERO asigna correctamente")
    void agregarRoles_adminYCajero_asignaCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setUsername("user1");
        usuario.setPassword("password");

        Rol rolCajero = new Rol("CAJERO");
        rolCajero.setId(1L);
        Rol rolAdmin = new Rol("ADMIN");
        rolAdmin.setId(2L);
        usuario.setRoles(Set.of(rolCajero, rolAdmin));

        assertThat(usuario.getRoles()).hasSize(2);
        assertThat(usuario.getRoles()).anyMatch(r -> r.getNombre().equals("CAJERO"));
        assertThat(usuario.getRoles()).anyMatch(r -> r.getNombre().equals("ADMIN"));
    }

    // ==================== AUTENTICACIÓN ====================

    @Test
    @DisplayName("Autenticar usuario existente retorna CustomUserDetails correctamente")
    void autenticar_usuarioExistente_retornaDetalles() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero");
        usuario.setPassword("$2a$10$hash");
        usuario.setRoles(Set.of(new Rol("CAJERO")));

        when(usuarioRepository.findByUsername("cajero")).thenReturn(Optional.of(usuario));

        var details = customUserDetailsService.loadUserByUsername("cajero");

        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("cajero");
    }

    @Test
    @DisplayName("Autenticar usuario inexistente lanza UsernameNotFoundException")
    void autenticar_usuarioInexistente_lanzaUsernameNotFoundException() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("fantasma");
    }

    @ParameterizedTest(name = "credential inválida: \"{0}\"")
    @MethodSource("credencialesInvalidas")
    @DisplayName("Credenciales inválidas (null, vacías, inexistentes) lanzan excepción")
    void credencialesInvalidas_lanzanExcepcion(String username) {
        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    static Stream<String> credencialesInvalidas() {
        return Stream.of("", "noexiste", "CAJERO_INVALIDO");
    }

    // ==================== AUTHORITIES (prefijo ROLE_) ====================

    @Test
    @DisplayName("Roles se mapean con prefijo ROLE_ en las authorities")
    void roles_mapeadosConPrefixROLE_enAuthorities() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cajero");
        usuario.setPassword("$2a$10$hash");
        usuario.setRoles(Set.of(new Rol("CAJERO")));

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CAJERO");
    }

    @Test
    @DisplayName("Usuario con múltiples roles tiene todas las authorities correspondientes")
    void usuarioConMultiplesRoles_tieneTodasLasAuthorities() {
        Rol rolAdmin = new Rol("ADMIN");
        rolAdmin.setId(1L);
        Rol rolCajero = new Rol("CAJERO");
        rolCajero.setId(2L);

        Usuario usuario = new Usuario();
        usuario.setUsername("superadmin");
        usuario.setPassword("$2a$10$hash");
        usuario.setRoles(Set.of(rolAdmin, rolCajero));

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CAJERO");
    }

    @Test
    @DisplayName("Usuario sin roles tiene lista de authorities vacía")
    void usuarioSinRoles_tieneAuthoritiesVacias() {
        Usuario usuario = new Usuario();
        usuario.setUsername("sinrol");
        usuario.setPassword("$2a$10$hash");
        usuario.setRoles(null);

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("CustomUserDetails.isEnabled y demás flags retornan true")
    void customUserDetails_flagsDeSeguridad_retornanTrue() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cajero");
        usuario.setPassword("$2a$10$hash");
        usuario.setRoles(Set.of());

        CustomUserDetails details = new CustomUserDetails(usuario);

        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }
}
