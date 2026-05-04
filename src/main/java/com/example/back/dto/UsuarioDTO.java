package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/UsuarioDTO.java

import com.example.back.model.Carrera;
import com.example.back.model.Usuario.Rol;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class UsuarioDTO {

    // ── Crear usuario (solo admin puede hacer esto) ───────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CrearRequest {
        @NotBlank(message = "El nombre es requerido")
        private String nombre;

        @NotBlank(message = "El correo es requerido")
        @Email(message = "El correo no tiene un formato válido")
        private String correo;

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        private String password;

        @NotNull(message = "El rol es requerido")
        private Rol rol;

        // Solo para estudiantes
        private String matricula;
        private Carrera carrera;

        // Solo para admins
        private String numeroEmpleado;
    }

    // ── Actualizar datos propios (estudiante actualiza su correo o contraseña) ─
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ActualizarRequest {
        @Email(message = "El correo no tiene un formato válido")
        private String correo;      // opcional
    }

    // ── Actualizar usuario por admin ──────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActualizarAdminRequest {
        private String nombre;
        private String correo;
        private String matricula;
        private Carrera carrera;
        private String numeroEmpleado;
        private Boolean activo;
    }

    // ── Respuesta con datos del usuario ───────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String nombre;
        private String correo;
        private Rol rol;
        private String matricula;
        private String numeroEmpleado;
        private Carrera carrera;
        private Boolean activo;
        private LocalDateTime fechaRegistro;
    }

    // ── Respuesta resumida (para listas) ──────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResumenResponse {
        private Long id;
        private String nombre;
        private String correo;
        private Rol rol;
        private String matricula;
        private Carrera carrera;
        private Boolean activo;
    }
}
