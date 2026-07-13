package com.minimarket.controller;

import com.minimarket.dto.CarritoDto;
import com.minimarket.dto.request.CarritoRequestDto;
import com.minimarket.service.CarritoService;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de autorización de CarritoController")
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(carritoService.findAll()).thenReturn(List.of());
        lenient().when(carritoService.findById(anyLong())).thenReturn(new CarritoDto());
        lenient().when(carritoService.save(any(CarritoRequestDto.class))).thenReturn(new CarritoDto());
        lenient().when(carritoService.update(anyLong(), any(CarritoRequestDto.class)))
                .thenReturn(Optional.of(new CarritoDto()));
    }

    // --- GET (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Listar carrito con rol CAJERO retorna 200")
    void listarCarrito_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Listar carrito con rol ADMIN retorna 200")
    void listarCarrito_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Listar carrito sin autenticación retorna 401")
    void listarCarrito_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "rol {0} puede acceder al carrito")
    @ValueSource(strings = {"CAJERO", "ADMIN"})
    @DisplayName("Roles autorizados acceden al carrito y retornan 200")
    void rolesAutorizados_puedenAcceder(String rol) throws Exception {
        mockMvc.perform(get("/api/carrito")
                        .with(user("testuser").roles(rol)))
                .andExpect(status().isOk());
    }

    // --- POST (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Agregar al carrito con rol CAJERO retorna 200")
    void agregarAlCarrito_conRolCajero_retorna200() throws Exception {
        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"productoId\":1,\"cantidad\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Agregar al carrito sin autenticación retorna 401")
    void agregarAlCarrito_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"productoId\":1,\"cantidad\":2}"))
                .andExpect(status().isUnauthorized());

        verify(carritoService, never()).save(any());
    }

    // --- PUT (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actualizar carrito con rol ADMIN retorna 404 cuando no existe")
    void actualizarCarrito_conRolAdmin_itemInexistente_retorna404() throws Exception {
        lenient().when(carritoService.update(anyLong(), any(CarritoRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/carrito/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"productoId\":1,\"cantidad\":3}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE (CAJERO o ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar ítem del carrito con rol ADMIN retorna 404 cuando no existe")
    void eliminarDelCarrito_conRolAdmin_itemInexistente_retorna404() throws Exception {
        when(carritoService.findById(999L)).thenReturn(null);
        mockMvc.perform(delete("/api/carrito/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar ítem del carrito con rol ADMIN retorna 204")
    void eliminarDelCarrito_conRolAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/carrito/999"))
                .andExpect(status().isNoContent());
    }

    // --- GET por id ---

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Obtener ítem inexistente del carrito retorna 404")
    void obtenerCarritoPorId_inexistente_retorna404() throws Exception {
        when(carritoService.findById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/carrito/999"))
                .andExpect(status().isNotFound());
    }
}
