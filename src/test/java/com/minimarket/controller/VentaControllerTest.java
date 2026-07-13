package com.minimarket.controller;

import com.minimarket.dto.VentaDto;
import com.minimarket.dto.request.VentaRequestDto;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de autorización de VentaController")
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService ventaService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(ventaService.findAll()).thenReturn(List.of());
        lenient().when(ventaService.findById(anyLong())).thenReturn(null);
        lenient().when(ventaService.save(any(VentaRequestDto.class))).thenReturn(new VentaDto());
    }

    // --- GET (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Listar ventas con rol CAJERO retorna 200")
    void listarVentas_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Listar ventas con rol ADMIN retorna 200")
    void listarVentas_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Listar ventas sin autenticación retorna 401")
    void listarVentas_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isUnauthorized());
    }

    // --- POST (solo CAJERO) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Crear venta con rol CAJERO retorna 200")
    void crearVenta_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"fecha\":\"2026-07-11\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Crear venta con rol ADMIN retorna 403 (solo CAJERO puede crear ventas)")
    void crearVenta_conRolAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"fecha\":\"2026-07-11\"}"))
                .andExpect(status().isForbidden());

        verify(ventaService, never()).save(any());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Crear venta sin autenticación retorna 401")
    void crearVenta_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"fecha\":\"2026-07-11\"}"))
                .andExpect(status().isUnauthorized());

        verify(ventaService, never()).save(any());
    }

    // --- GET por id ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Obtener venta inexistente retorna 404")
    void obtenerVenta_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/ventas/999"))
                .andExpect(status().isNotFound());
    }
}
