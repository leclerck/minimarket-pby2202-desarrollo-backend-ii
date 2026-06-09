package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Inicializa roles y usuarios de prueba al arrancar la aplicación.
 * Las contraseñas se almacenan con hash BCrypt.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol userRole = findOrCreateRole("USER");
        Rol staffRole = findOrCreateRole("STAFF");
        Rol adminRole = findOrCreateRole("ADMIN");

        createUserIfMissing("cliente", "cliente123", userRole);
        createUserIfMissing("staff", "staff123", staffRole);
        createUserIfMissing("admin", "admin123", adminRole);
    }

    private Rol findOrCreateRole(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(nombre);
                    return rolRepository.save(rol);
                });
    }

    /** Crea el usuario solo si no existe (evita duplicados en reinicios). */
    private void createUserIfMissing(String username, String rawPassword, Rol rol) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuario.setRoles(Set.of(rol));
            usuarioRepository.save(usuario);
        }
    }
}
