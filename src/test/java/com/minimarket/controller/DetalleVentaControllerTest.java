package com.minimarket.controller;

import com.minimarket.dto.DetalleVentaDto;
import com.minimarket.dto.request.DetalleVentaRequestDto;
import com.minimarket.service.DetalleVentaService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de autorización de DetalleVentaController")
class DetalleVentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetalleVentaService detalleVentaService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(detalleVentaService.findAll()).thenReturn(List.of());
        lenient().when(detalleVentaService.findById(anyLong())).thenReturn(new DetalleVentaDto());
        lenient().when(detalleVentaService.save(any(DetalleVentaRequestDto.class))).thenReturn(new DetalleVentaDto());
        lenient().when(detalleVentaService.update(anyLong(), any(DetalleVentaRequestDto.class)))
                .thenReturn(Optional.of(new DetalleVentaDto()));
    }

    // --- GET (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Listar detalles con rol CAJERO retorna 200")
    void listarDetalles_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Listar detalles con rol ADMIN retorna 200")
    void listarDetalles_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Listar detalles sin autenticación retorna 401")
    void listarDetalles_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isUnauthorized());
    }

    // --- POST (solo CAJERO) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Guardar detalle con rol CAJERO retorna 200")
    void guardarDetalle_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ventaId\":1,\"productoId\":1,\"cantidad\":2,\"precio\":500}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Guardar detalle con rol ADMIN retorna 403 (solo CAJERO puede crear detalles)")
    void guardarDetalle_conRolAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ventaId\":1,\"productoId\":1,\"cantidad\":2,\"precio\":500}"))
                .andExpect(status().isForbidden());

        verify(detalleVentaService, never()).save(any());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Guardar detalle sin autenticación retorna 401")
    void guardarDetalle_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ventaId\":1,\"productoId\":1,\"cantidad\":2,\"precio\":500}"))
                .andExpect(status().isUnauthorized());

        verify(detalleVentaService, never()).save(any());
    }

    // --- PUT (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actualizar detalle con rol autorizado retorna 404 cuando no existe")
    void actualizarDetalle_conRolAdmin_detalleInexistente_retorna404() throws Exception {
        lenient().when(detalleVentaService.update(anyLong(), any(DetalleVentaRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/detalle-ventas/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":5,\"precio\":600}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar detalle con rol ADMIN retorna 404 cuando no existe")
    void eliminarDetalle_conRolAdmin_detalleInexistente_retorna404() throws Exception {
        when(detalleVentaService.findById(999L)).thenReturn(null);
        mockMvc.perform(delete("/api/detalle-ventas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar detalle con rol ADMIN retorna 204")
    void eliminarDetalle_conRolAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/detalle-ventas/999"))
                .andExpect(status().isNoContent());
    }

    // --- GET por id ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Obtener detalle inexistente retorna 404")
    void obtenerDetalle_inexistente_retorna404() throws Exception {
        when(detalleVentaService.findById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/detalle-ventas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Obtener detalle con rol ADMIN retorna 200")
    void obtenerDetallePorId_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Obtener detalle sin autenticación retorna 401")
    void obtenerDetallePorId_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/detalle-ventas/1"))
                .andExpect(status().isUnauthorized());
    }
}
