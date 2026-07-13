package com.minimarket.controller;

import com.minimarket.dto.InventarioDto;
import com.minimarket.dto.request.InventarioRequestDto;
import com.minimarket.service.InventarioService;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de autorización de InventarioController")
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(inventarioService.findAll()).thenReturn(List.of());
        lenient().when(inventarioService.findById(anyLong())).thenReturn(new InventarioDto());
        lenient().when(inventarioService.save(any(InventarioRequestDto.class))).thenReturn(new InventarioDto());
        lenient().when(inventarioService.update(anyLong(), any(InventarioRequestDto.class))).thenReturn(Optional.of(new InventarioDto()));
    }

    // --- GET (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Listar movimientos con rol CAJERO retorna 200")
    void listarMovimientos_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Listar movimientos con rol ADMIN retorna 200")
    void listarMovimientos_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Listar movimientos sin autenticación retorna 401")
    void listarMovimientos_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "rol {0} puede acceder a inventario")
    @ValueSource(strings = {"CAJERO", "ADMIN"})
    @DisplayName("Roles autorizados acceden a inventario y retornan 200")
    void rolesAutorizados_retornan200(String rol) throws Exception {
        mockMvc.perform(get("/api/inventario")
                        .with(user("testuser").roles(rol)))
                .andExpect(status().isOk());
    }

    // --- GET por id ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Buscar movimiento por id con rol CAJERO retorna 200")
    void buscarMovimientoPorId_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Buscar movimiento por id con rol ADMIN retorna 200")
    void buscarMovimientoPorId_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Buscar movimiento por id sin autenticación retorna 401")
    void buscarMovimientoPorId_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "id={0}")
    @WithMockUser(roles = "CAJERO")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Buscar movimiento por ids límite con usuario autenticado retorna 404")
    void buscarMovimientoPorId_idsLimite_retorna404(long id) throws Exception {
        when(inventarioService.findById(id)).thenReturn(null);
        mockMvc.perform(get("/api/inventario/" + id))
                .andExpect(status().isNotFound());
    }

    // --- POST (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Registrar movimiento con rol CAJERO retorna 200")
    void registrarMovimiento_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Registrar movimiento con rol ADMIN retorna 200")
    void registrarMovimiento_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Registrar movimiento sin autenticación retorna 401")
    void registrarMovimiento_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isUnauthorized());

        verify(inventarioService, never()).save(any());
    }

    // --- PUT (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Actualizar movimiento con rol CAJERO retorna 200")
    void actualizarMovimiento_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(put("/api/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actualizar movimiento con rol ADMIN retorna 200")
    void actualizarMovimiento_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(put("/api/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Actualizar movimiento sin autenticación retorna 401")
    void actualizarMovimiento_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(put("/api/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "id={0}")
    @WithMockUser(roles = "CAJERO")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Actualizar movimiento por ids límite con usuario autenticado retorna 404")
    void actualizarMovimientoPorId_idsLimite_retorna404(long id) throws Exception {
        when(inventarioService.update(eq(id), any(InventarioRequestDto.class))).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/inventario/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\"}"))
                .andExpect(status().isNotFound());
    }

    // DELETE (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Eliminar movimiento con rol CAJERO retorna 204")
    void eliminarMovimiento_conRolCajero_retorna204() throws Exception {
        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar movimiento con rol ADMIN retorna 204")
    void eliminarMovimiento_conRolAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Eliminar movimiento sin autenticación retorna 401")
    void eliminarMovimiento_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "id={0}")
    @WithMockUser(roles = "CAJERO")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @DisplayName("Eliminar movimiento por ids límite con usuario autenticado retorna 404")
    void eliminarMovimientoPorId_idsLimite_retorna404(long id) throws Exception {
        when(inventarioService.findById(id)).thenReturn(null);
        mockMvc.perform(delete("/api/inventario/" + id))
                .andExpect(status().isNotFound());
    }
}
