package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/AuthController.java

import com.example.back.dto.AuthDTO;
import com.example.back.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}
