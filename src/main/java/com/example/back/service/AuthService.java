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

    // ── Login ─────────────────────────────────────────────────────────────────
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        // Autentica — lanza excepción si las credenciales son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreo(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

    // ── Cambiar contraseña (el propio usuario autenticado) ────────────────────
    public void cambiarPassword(String correoUsuario, AuthDTO.CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }
}
