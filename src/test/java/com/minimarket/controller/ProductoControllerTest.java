package com.minimarket.controller;

import com.minimarket.dto.ProductoDto;
import com.minimarket.dto.request.ProductoRequestDto;
import com.minimarket.service.ProductoService;
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
@DisplayName("Pruebas de autorización de ProductoController")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(productoService.findAll()).thenReturn(List.of());
        lenient().when(productoService.findById(anyLong())).thenReturn(new ProductoDto());
        lenient().when(productoService.save(any(ProductoRequestDto.class))).thenReturn(new ProductoDto());
        lenient().when(productoService.update(anyLong(), any(ProductoRequestDto.class)))
                .thenReturn(Optional.of(new ProductoDto()));
    }

    // --- GET público ---

    @Test
    @WithAnonymousUser
    @DisplayName("Listar productos sin autenticación retorna 200 (endpoint público)")
    void listarProductos_sinAutenticacion_retorna200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Buscar producto por id sin autenticación retorna 200")
    void buscarProductoPorId_sinAutenticacion_retorna200() throws Exception {
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk());
    }

    // --- POST (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Crear producto con rol ADMIN retorna 200")
    void crearProducto_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Leche\",\"precio\":1200,\"stock\":50}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Crear producto con rol CAJERO retorna 403")
    void crearProducto_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Leche\",\"precio\":1200,\"stock\":50}"))
                .andExpect(status().isForbidden());

        verify(productoService, never()).save(any());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Crear producto sin autenticación retorna 401")
    void crearProducto_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Leche\",\"precio\":1200,\"stock\":50}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "rol {0} no puede escribir productos")
    @ValueSource(strings = {"CAJERO"})
    @DisplayName("Roles no autorizados para escritura de productos retornan 403")
    void escritura_rolNoAdmin_retorna403(String rol) throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(user("testuser").roles(rol))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // --- PUT (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actualizar producto con rol ADMIN retorna 404 cuando no existe")
    void actualizarProducto_conRolAdmin_productoInexistente_retorna404() throws Exception {
        lenient().when(productoService.update(anyLong(), any(ProductoRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Leche\",\"precio\":1200,\"stock\":50}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Actualizar producto con rol CAJERO retorna 403")
    void actualizarProducto_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Leche\",\"precio\":1200,\"stock\":50}"))
                .andExpect(status().isForbidden());
    }

    // --- DELETE (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar producto con rol ADMIN retorna 404 cuando no existe")
    void eliminarProducto_conRolAdmin_productoInexistente_retorna404() throws Exception {
        when(productoService.findById(999L)).thenReturn(null);
        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Eliminar producto con rol CAJERO retorna 403")
    void eliminarProducto_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar producto con rol ADMIN retorna 204")
    void eliminarProducto_conRolAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isNoContent());
    }
}
