package com.minimarket.service;

import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.UsuarioDto;
import com.minimarket.dto.request.UsuarioRequestDto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioValido() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("cajero");
        u.setPassword("clave123");
        return u;
    }

    private UsuarioRequestDto requestValido() {
        UsuarioRequestDto r = new UsuarioRequestDto();
        r.setUsername("cajero");
        r.setPassword("clave123");
        return r;
    }

    static Stream<String> usernamesInvalidos() {
        return Stream.of(null, "", "   ");
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar usuarios retorna todos los registros como DTOs")
    void listarUsuarios_retornaListaCompleta() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioValido()));
        when(dtoMapper.toUsuarioDtos(any())).thenReturn(List.of(new UsuarioDto()));

        List<UsuarioDto> result = usuarioService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Listar usuarios: repositorio vacío retorna lista vacía")
    void listarUsuarios_repositorioVacio_retornaListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(List.of());
        when(dtoMapper.toUsuarioDtos(any())).thenReturn(List.of());

        List<UsuarioDto> result = usuarioService.findAll();

        assertThat(result).isEmpty();
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar usuario existente retorna DTO")
    void buscarPorId_usuarioExistente_retornaDto() {
        Usuario usuario = usuarioValido();
        UsuarioDto dto = new UsuarioDto();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(dtoMapper.toDto(usuario)).thenReturn(dto);

        UsuarioDto result = usuarioService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar usuario inexistente retorna null")
    void buscarPorId_usuarioInexistente_retornaNull() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThat(usuarioService.findById(99L)).isNull();
    }

    @ParameterizedTest(name = "id={0}")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Buscar por ids límite retorna null")
    void buscarPorId_idsLimite_retornaNull(long id) {
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        UsuarioDto result = usuarioService.findById(id);

        assertThat(result).isNull();
    }

    // --- findByUsername ---

    @Test
    @DisplayName("Buscar por username existente retorna la entidad Usuario")
    void buscarPorUsername_usuarioExistente_retornaUsuario() {
        Usuario usuario = usuarioValido();
        when(usuarioRepository.findByUsername("cajero")).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.findByUsername("cajero");

        assertThat(result).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Buscar por username inexistente retorna null")
    void buscarPorUsername_usuarioInexistente_retornaNull() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThat(usuarioService.findByUsername("noexiste")).isNull();
    }

    // --- save (contraseña) ---

    @Test
    @DisplayName("Guardar usuario con contraseña plana la codifica con BCrypt")
    void guardarUsuario_contraseniaPlana_codificaConBCrypt() {
        Usuario usuario = usuarioValido();
        usuario.setPassword("clave123");
        when(dtoMapper.toEntity(any(UsuarioRequestDto.class))).thenReturn(usuario);
        when(passwordEncoder.encode("clave123")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dtoMapper.toDto(any(Usuario.class))).thenReturn(new UsuarioDto());

        usuarioService.save(requestValido());

        verify(passwordEncoder).encode("clave123");
        assertThat(usuario.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("Guardar usuario con contraseña ya hasheada no la recodifica")
    void guardarUsuario_contraseniaYaHasheada_noRecodifica() {
        Usuario usuario = usuarioValido();
        usuario.setPassword("$2a$10$hashexistente");
        when(dtoMapper.toEntity(any(UsuarioRequestDto.class))).thenReturn(usuario);
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(dtoMapper.toDto(any(Usuario.class))).thenReturn(new UsuarioDto());

        usuarioService.save(requestValido());

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Guardar usuario con contraseña nula guarda sin hashear")
    void guardarUsuario_contraseniaNula_guardaSinHashear() {
        Usuario usuario = usuarioValido();
        usuario.setPassword(null);
        when(dtoMapper.toEntity(any(UsuarioRequestDto.class))).thenReturn(usuario);
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(dtoMapper.toDto(any(Usuario.class))).thenReturn(new UsuarioDto());

        usuarioService.save(requestValido());

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository).save(usuario);
    }

    // --- save (inválidos) → verify(never()) ---

    @Test
    @DisplayName("Guardar request nulo lanza excepción y no llama save")
    void guardarUsuario_requestNulo_lanzaExcepcion() {
        assertThatThrownBy(() -> usuarioService.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

        verify(usuarioRepository, never()).save(any());
    }

    @ParameterizedTest(name = "username inválido: \"{0}\"")
    @MethodSource("usernamesInvalidos")
    @DisplayName("Usernames inválidos (null, vacío, espacios) lanzan excepción y no llaman save")
    void usernames_invalidos_lanzanExcepcion(String username) {
        Usuario usuarioConUsernameInvalido = usuarioValido();
        usuarioConUsernameInvalido.setUsername(username);
        when(dtoMapper.toEntity(any(UsuarioRequestDto.class))).thenReturn(usuarioConUsernameInvalido);

        UsuarioRequestDto request = requestValido();
        assertThatThrownBy(() -> usuarioService.save(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    // --- deleteById ---

    @Test
    @DisplayName("Eliminar usuario delega al repositorio")
    void eliminarUsuario_delegaAlRepositorio() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.deleteById(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(usuarioRepository.findAll()).thenThrow(new RuntimeException("BD no disponible"));

        assertThatThrownBy(() -> usuarioService.findAll())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("DataAccessException del repositorio se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Usuario usuario = usuarioValido();
        when(dtoMapper.toEntity(any(UsuarioRequestDto.class))).thenReturn(usuario);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hash");
        when(usuarioRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("Username duplicado"));

        UsuarioRequestDto request = requestValido();
        assertThatThrownBy(() -> usuarioService.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
