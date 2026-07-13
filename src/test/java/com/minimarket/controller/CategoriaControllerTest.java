package com.minimarket.controller;

import com.minimarket.dto.CategoriaDto;
import com.minimarket.dto.request.CategoriaRequestDto;
import com.minimarket.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de autorización de CategoriaController")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(categoriaService.findAll()).thenReturn(List.of());
        lenient().when(categoriaService.findById(anyLong())).thenReturn(new CategoriaDto());
        lenient().when(categoriaService.save(any(CategoriaRequestDto.class))).thenReturn(new CategoriaDto());
        lenient().when(categoriaService.update(anyLong(), any(CategoriaRequestDto.class)))
                .thenReturn(Optional.of(new CategoriaDto()));
    }

    // --- GET público ---

    @Test
    @WithAnonymousUser
    @DisplayName("Listar categorías sin autenticación retorna 200 (endpoint público)")
    void listarCategorias_sinAutenticacion_retorna200() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Buscar categoría por id sin autenticación retorna 200")
    void buscarCategoriaPorId_sinAutenticacion_retorna200() throws Exception {
        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Buscar categoría por id inexistente retorna 404")
    void buscarCategoriaPorId_inexistente_retorna404() throws Exception {
        when(categoriaService.findById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/categorias/999"))
                .andExpect(status().isNotFound());
    }

    // --- POST (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Guardar categoría con rol ADMIN retorna 200")
    void guardarCategoria_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Lácteos\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Guardar categoría con rol CAJERO retorna 403")
    void guardarCategoria_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Lácteos\"}"))
                .andExpect(status().isForbidden());

        verify(categoriaService, never()).save(any());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Guardar categoría sin autenticación retorna 401")
    void guardarCategoria_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Lácteos\"}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "rol {0} no puede escribir categorías")
    @ValueSource(strings = {"CAJERO"})
    @DisplayName("Roles no autorizados para escritura de categorías retornan 403")
    void rolesNoPermitidosParaEscritura_retornan403(String rol) throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .with(user("testuser").roles(rol))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // --- PUT (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actualizar categoría con rol ADMIN retorna 404 cuando no existe")
    void actualizarCategoria_conRolAdmin_categoriaInexistente_retorna404() throws Exception {
        lenient().when(categoriaService.update(anyLong(), any(CategoriaRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/categorias/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Actualizar categoría con rol CAJERO retorna 403")
    void actualizarCategoria_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isForbidden());
    }

    // --- DELETE (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar categoría con rol ADMIN retorna 404 cuando no existe")
    void eliminarCategoria_conRolAdmin_categoriaInexistente_retorna404() throws Exception {
        when(categoriaService.findById(999L)).thenReturn(null);
        mockMvc.perform(delete("/api/categorias/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Eliminar categoría con rol CAJERO retorna 403")
    void eliminarCategoria_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar categoría con rol ADMIN retorna 204")
    void eliminarCategoria_conRolAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/categorias/999"))
                .andExpect(status().isNoContent());
    }
}
