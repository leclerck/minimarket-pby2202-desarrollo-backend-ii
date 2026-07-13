package com.minimarket.service;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.DtoMapper;
import com.minimarket.dto.request.CategoriaRequestDto;
import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de CategoriaService")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoriaValida() {
        Categoria c = new Categoria();
        c.setId(1L);
        c.setNombre("Lácteos");
        return c;
    }

    private CategoriaRequestDto requestValido() {
        CategoriaRequestDto r = new CategoriaRequestDto();
        r.setNombre("Lácteos");
        return r;
    }

    static Stream<String> nombresInvalidos() {
        return Stream.of(null, "", "   ");
    }

    // --- findAll ---

    @Test
    @DisplayName("Listar categorías retorna todos los registros como DTOs")
    void listar_retornaTodasLasCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaValida(), categoriaValida()));
        when(dtoMapper.toCategoriaDtos(any())).thenReturn(List.of(new CategoriaDto(), new CategoriaDto()));

        List<CategoriaDto> result = categoriaService.findAll();

        assertThat(result).hasSize(2);
    }

    // --- findById ---

    @Test
    @DisplayName("Buscar categoría existente retorna el DTO")
    void buscarPorId_existente_retornaDto() {
        Categoria categoria = categoriaValida();
        CategoriaDto dto = new CategoriaDto();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(dtoMapper.toDto(categoria)).thenReturn(dto);

        CategoriaDto result = categoriaService.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Buscar categoría inexistente retorna null")
    void buscarPorId_inexistente_retornaNull() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThat(categoriaService.findById(99L)).isNull();
    }

    // --- save (válido) ---

    @Test
    @DisplayName("Guardar categoría con nombre válido guarda correctamente y retorna DTO")
    void guardarCategoria_nombreValido_guardaCorrectamente() {
        Categoria categoria = categoriaValida();
        CategoriaDto dto = new CategoriaDto();
        when(dtoMapper.toEntity(any(CategoriaRequestDto.class))).thenReturn(categoria);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(dtoMapper.toDto(categoria)).thenReturn(dto);

        CategoriaDto result = categoriaService.save(requestValido());

        assertThat(result).isEqualTo(dto);
        verify(categoriaRepository).save(categoria);
    }

    // --- save (inválidos) → verify(never()) ---

    @Test
    @DisplayName("Guardar request nula lanza excepción y no llama save")
    void guardarCategoria_requestNula_lanzaExcepcion() {
        assertThatThrownBy(() -> categoriaService.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nula");

        verify(categoriaRepository, never()).save(any());
    }

    @ParameterizedTest(name = "nombre inválido: \"{0}\"")
    @MethodSource("nombresInvalidos")
    @DisplayName("Nombres inválidos (null, vacío, espacios) lanzan excepción y no llaman save")
    void nombresInvalidos_lanzanExcepcion(String nombre) {
        Categoria categoriaConNombreInvalido = categoriaValida();
        categoriaConNombreInvalido.setNombre(nombre);
        when(dtoMapper.toEntity(any(CategoriaRequestDto.class))).thenReturn(categoriaConNombreInvalido);

        CategoriaRequestDto request = requestValido();
        assertThatThrownBy(() -> categoriaService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");

        verify(categoriaRepository, never()).save(any());
    }

    // --- deleteById ---

    @Test
    @DisplayName("Eliminar categoría delega al repositorio")
    void eliminar_delegaAlRepositorio() {
        doNothing().when(categoriaRepository).deleteById(1L);

        categoriaService.deleteById(1L);

        verify(categoriaRepository).deleteById(1L);
    }

    // --- Fallos del repositorio ---

    @Test
    @DisplayName("RuntimeException del repositorio se propaga")
    void repositorio_lanzaRuntimeException_propagaExcepcion() {
        when(categoriaRepository.findAll()).thenThrow(new RuntimeException("Error inesperado"));

        assertThatThrownBy(() -> categoriaService.findAll())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("DataAccessException se propaga desde save")
    void repositorio_lanzaDataAccessException_propagaExcepcion() {
        Categoria categoria = categoriaValida();
        when(dtoMapper.toEntity(any(CategoriaRequestDto.class))).thenReturn(categoria);
        when(categoriaRepository.save(categoria))
                .thenThrow(new DataIntegrityViolationException("Nombre duplicado"));

        CategoriaRequestDto request = requestValido();
        assertThatThrownBy(() -> categoriaService.save(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
