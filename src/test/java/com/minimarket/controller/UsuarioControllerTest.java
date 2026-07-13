package com.minimarket.controller;

import com.minimarket.dto.UsuarioDto;
import com.minimarket.dto.request.UsuarioRequestDto;
import com.minimarket.service.UsuarioService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de autorización de UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @BeforeEach
    void configurarMocks() {
        lenient().when(usuarioService.findAll()).thenReturn(List.of());
        lenient().when(usuarioService.findById(anyLong())).thenReturn(new UsuarioDto());
        lenient().when(usuarioService.save(any(UsuarioRequestDto.class))).thenReturn(new UsuarioDto());
        lenient().when(usuarioService.update(anyLong(), any(UsuarioRequestDto.class)))
                .thenReturn(Optional.of(new UsuarioDto()));
    }

    // --- GET (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Listar usuarios con rol ADMIN retorna 200")
    void listarUsuarios_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Listar usuarios con rol CAJERO retorna 403")
    void listarUsuarios_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Listar usuarios sin autenticación retorna 401")
    void listarUsuarios_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "rol {0} no puede gestionar usuarios")
    @ValueSource(strings = {"CAJERO"})
    @DisplayName("Roles no autorizados para gestión de usuarios retornan 403")
    void rolNoAdmin_retorna403(String rol) throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .with(user("testuser").roles(rol)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Buscar usuario por id con rol ADMIN retorna 200")
    void buscarUsuarioPorId_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Buscar usuario por id con rol CAJERO retorna 403")
    void buscarUsuarioPorId_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Buscar usuario por id sin autenticación retorna 401")
    void buscarUsuarioPorId_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "id={0}")
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Buscar usuario por ids límite retorna 404")
    void buscarUsuarioPorId_idsLimite_retorna404(long id) throws Exception {
        when(usuarioService.findById(id)).thenReturn(null);
        mockMvc.perform(get("/api/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    // --- POST (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Crear usuario con rol ADMIN retorna 200")
    void crearUsuario_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo\",\"password\":\"clave123\",\"rolIds\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Crear usuario con rol CAJERO retorna 403")
    void crearUsuario_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo\",\"password\":\"clave123\"}"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).save(any());
    }

    // --- PUT (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actualizar usuario con rol ADMIN retorna 200")
    void actualizarUsuario_conRolAdmin_retorna200() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo\",\"password\":\"clave123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Actualizar usuario con rol CAJERO retorna 403")
    void actualizarUsuario_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nuevo\",\"password\":\"clave123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Actualizar usuario sin autenticación retorna 401")
    void actualizarUsuario_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nuevo\",\"password\":\"clave123\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE (solo ADMIN) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Eliminar usuario inexistente con rol ADMIN retorna 404")
    void eliminarUsuario_conRolAdmin_usuarioInexistente_retorna404() throws Exception {
        when(usuarioService.findById(999L)).thenReturn(null);
        mockMvc.perform(delete("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    @DisplayName("Eliminar usuario con rol CAJERO retorna 403")
    void eliminarUsuario_conRolCajero_retorna403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).deleteById(anyLong());
    }
}
