package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/AuthDTO.java

import com.example.back.model.Carrera;
import com.example.back.model.Usuario.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDTO {

    // ── Login request ─────────────────────────────────────────────────────────
    // El campo "correo" acepta tanto correo electrónico como matrícula.
    // La lógica de distinguir cuál es cuál está en AuthService.
    // Por eso se quitó @Email — una matrícula como "MAT-001" no pasaría esa validación.
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "El correo o matrícula es requerido")
        private String correo; // puede ser correo (admin) o matrícula (estudiante)

        @NotBlank(message = "La contraseña es requerida")
        private String password;
    }

    // ── Login response ────────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginResponse {
        private String token;
        private String tipo;
        private Long id;
        private String nombre;
        private String correo;
        private Rol rol;
        private Carrera carrera;
        private String matricula;
    }

    // ── Cambiar contraseña ────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CambiarPasswordRequest {
        @NotBlank(message = "La contraseña actual es requerida")
        private String passwordActual;

        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        private String passwordNueva;
    }
}