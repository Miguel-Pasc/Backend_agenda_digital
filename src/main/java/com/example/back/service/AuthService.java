package com.example.back.service;

// 📁 src/main/java/com/example/back/service/AuthService.java

import com.example.back.dto.AuthDTO;
import com.example.back.model.Usuario;
import com.example.back.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ── Login por número de empleado (admin) o matrícula (estudiante) ─────────
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        String identificador = request.getCorreo().trim();

        // Bloquear acceso directo con correo electrónico
        if (identificador.contains("@")) {
            throw new RuntimeException(
                    "Acceso no permitido con correo. " +
                            "Los administradores deben usar su número de empleado y " +
                            "los estudiantes su matrícula."
            );
        }

        // Buscar usuario: primero por número de empleado, luego por matrícula
        Usuario usuario = usuarioRepository.findByNumeroEmpleado(identificador)
                .or(() -> usuarioRepository.findByMatricula(identificador))
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró ningún usuario con el identificador: " + identificador));

        // Autenticar con Spring Security usando el correo interno
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usuario.getCorreo(), request.getPassword())
        );

        return AuthDTO.LoginResponse.builder()
                .token(jwtService.generarToken(usuario))
                .tipo("Bearer")
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .carrera(usuario.getCarrera())
                .matricula(usuario.getMatricula())
                .build();
    }

    // ── Cambiar contraseña ────────────────────────────────────────────────────
    public void cambiarPassword(String correoUsuario, AuthDTO.CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }
}