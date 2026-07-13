package com.minimarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.security.model.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración para verificar autenticación JWT y control de acceso por roles. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtenerToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("Acceso sin token retorna 401")
    void accesoSinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cajero puede acceder a inventario")
    void cajeroAccedeAInventario_retorna200() throws Exception {
        String token = obtenerToken("cajero", "cajero123");

        mockMvc.perform(get("/api/inventario")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cajero no puede crear productos (solo ADMIN)")
    void cajeroNoPuedeCrearProducto_retorna403() throws Exception {
        String token = obtenerToken("cajero", "cajero123");

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"ProductoPrueba\",\"precio\":10,\"stock\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Credenciales incorrectas retornan 401")
    void credencialesIncorrectas_retorna401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("cajero");
        loginRequest.setPassword("contraseña_incorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
