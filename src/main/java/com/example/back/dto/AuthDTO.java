package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/AuthDTO.java

import com.example.back.model.Carrera;
import com.example.back.model.Usuario.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDTO {

    // ── Lo que manda el cliente al hacer login ────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "El correo es requerido")
        @Email(message = "El correo no tiene un formato válido")
        private String correo;

        @NotBlank(message = "La contraseña es requerida")
        private String password;
    }

    // ── Lo que devuelve el servidor tras un login exitoso ─────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginResponse {
        private String token;
        private String tipo;        // "Bearer"
        private Long id;
        private String nombre;
        private String correo;
        private Rol rol;
        private Carrera carrera;    // null si es admin
        private String matricula;   // null si es admin
    }

    // ── Para cambiar contraseña (el propio usuario) ───────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CambiarPasswordRequest {
        @NotBlank(message = "La contraseña actual es requerida")
        private String passwordActual;

        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        private String passwordNueva;
    }
}
