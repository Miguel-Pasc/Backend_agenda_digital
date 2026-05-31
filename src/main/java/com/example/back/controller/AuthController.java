package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/AuthController.java

import com.example.back.dto.AuthDTO;
import com.example.back.dto.PasswordResetTokenDTO;
import com.example.back.service.AuthService;
import com.example.back.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    // POST /api/auth/login — público
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.LoginResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // PUT /api/auth/cambiar-password — estudiante y admin autenticados
    // Principal contiene el correo del usuario autenticado extraído del JWT
    @PutMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody AuthDTO.CambiarPasswordRequest request,
            Principal principal) {
        authService.cambiarPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperarPassword(
            @RequestBody @Valid PasswordResetTokenDTO.RecuperarPasswordRequest request) {
        // Siempre respuesta genérica para no revelar si el correo existe
        passwordResetService.solicitarRecuperacion(request.getCorreo());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo está registrado, recibirás un enlace en breve."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody @Valid PasswordResetTokenDTO.ResetPasswordRequest request) {
        try {
            passwordResetService.resetearPassword(
                    request.getToken(),
                    request.getNuevaPassword()
            );
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
