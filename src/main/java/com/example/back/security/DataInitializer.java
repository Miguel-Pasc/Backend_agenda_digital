package com.example.back.security;

// 📁 src/main/java/com/example/back/security/DataInitializer.java
//
// Se ejecuta automáticamente al arrancar Spring Boot.
// Crea el admin por defecto si no existe ninguno en la BD.

import com.example.back.model.Usuario;
import com.example.back.model.Usuario.Rol;
import com.example.back.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByCorreo("admin@semana.com")) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .correo("admin@semana.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMIN)
                    .numeroEmpleado("EMP-001")
                    .build();
            usuarioRepository.save(admin);
            log.info("========================================");
            log.info("Admin creado por defecto:");
            log.info("  Correo:     admin@semana.com");
            log.info("  Contraseña: admin123");
            log.info("⚠️  Cambia la contraseña antes de subir a producción");
            log.info("========================================");
        }
    }
}
