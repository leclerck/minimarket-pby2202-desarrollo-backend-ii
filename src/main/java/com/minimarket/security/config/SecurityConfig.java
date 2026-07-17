package com.minimarket.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.minimarket.security.filter.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import java.util.Map;

/**
 * 
 * Configuración central de Spring Security.
 * 
 * Define autenticación stateless con JWT, rutas públicas y codificación BCrypt.
 * 
 */

@Configuration

@EnableMethodSecurity // Habilita @PreAuthorize en controladores

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;

    }

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // CSRF deshabilitado: la API usa JWT en header, no cookies de sesión

                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesión en servidor; cada petición se autentica con el token JWT

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/public/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()

                        // Catálogo visible sin autenticación (solo lectura)

                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()

                        .anyRequest().authenticated()

                )

                // Respuestas JSON en lugar de redirecciones HTML

                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint(this::handleUnauthorized)

                        .accessDeniedHandler(this::handleForbidden)

                )

                // El filtro JWT se ejecuta antes del filtro de login por formulario

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean

    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {

        return authConfig.getAuthenticationManager();

    }

    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    /** Respuesta 401 cuando no hay token o es inválido. */

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response,

            org.springframework.security.core.AuthenticationException authException)

            throws IOException {

        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");

    }

    /** Respuesta 403 cuando el usuario no tiene el rol requerido. */

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response,

            org.springframework.security.access.AccessDeniedException accessDeniedException)

            throws IOException {

        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");

    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {

        response.setStatus(status);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        new ObjectMapper().writeValue(response.getOutputStream(), Map.of("error", message));

    }

}
