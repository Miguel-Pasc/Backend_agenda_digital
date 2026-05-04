package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/SemanaAcademicaDTO.java

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

public class SemanaAcademicaDTO {

    // ── Crear nueva semana académica (solo admin) ─────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CrearRequest {
        @NotNull(message = "El número de semana es requerido")
        @Min(value = 1, message = "El número de semana debe ser mayor a 0")
        private Integer numero;

        @NotNull(message = "El año es requerido")
        @Min(value = 2020, message = "El año no es válido")
        private Integer anio;

        @NotNull(message = "La fecha de inicio es requerida")
        private LocalDate fechaInicio;

        @NotNull(message = "La fecha de fin es requerida")
        private LocalDate fechaFin;
    }

    // ── Respuesta completa ────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Integer numero;
        private Integer anio;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Boolean activa;
        private Integer duracionDias;       // calculado: fechaFin - fechaInicio + 1
        private Integer totalConferencias;  // calculado
    }

    // ── Respuesta resumida (para historial) ───────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResumenResponse {
        private Long id;
        private Integer numero;
        private Integer anio;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Boolean activa;
    }
}
