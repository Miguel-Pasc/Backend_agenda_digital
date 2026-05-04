package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/ConferencistaDTO.java

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class ConferencistaDTO {

    // ── Crear / agregar conferencista ─────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "El nombre del conferencista es requerido")
        private String nombre;

        private String perfilProfesional;
        private String biografia;
        private String fotografiaUrl;   // null si aún no se tiene
        private String logoUrl;         // null si aún no se tiene
    }

    // ── Actualizar conferencista (puede ser solo foto o logo) ─────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActualizarRequest {
        private String nombre;
        private String perfilProfesional;
        private String biografia;
        private String fotografiaUrl;
        private String logoUrl;
    }

    // ── Respuesta ─────────────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String nombre;
        private String perfilProfesional;
        private String biografia;
        private String fotografiaUrl;
        private String logoUrl;
    }
}
