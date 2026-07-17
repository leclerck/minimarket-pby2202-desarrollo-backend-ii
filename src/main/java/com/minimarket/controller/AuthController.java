package com.minimarket.controller;

import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.model.LoginResponse;
import com.minimarket.security.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de autenticación.
 * Valida credenciales contra la base de datos y devuelve un token JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "API para autenticación de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint de login. Recibe username/password y retorna un JWT si son válidos.
     * Ruta pública (no requiere token).
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login de usuario", tags = "Autenticación")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token, jwtUtil.getExpiration()));
    }
}
