package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/InscripcionDTO.java

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class InscripcionDTO {

    // ── Inscribir estudiante a una conferencia ────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private Long conferenciaId;
    }

    // ── Respuesta de inscripción ───────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long estudianteId;
        private String estudianteNombre;
        private Long conferenciaId;
        private String conferenciaNombre;
        private Integer conferenciaDia;
        private LocalDate conferenciaFechaReal;
        private LocalTime conferenciaHoraInicio;
        private LocalTime conferenciaHoraFin;
        private LocalDateTime fechaInscripcion;
    }

    // ── Item de agenda personal del estudiante ────────────────────────────────
    // Versión compacta para mostrar en la agenda
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AgendaItem {
        private Long inscripcionId;
        private Long conferenciaId;
        private String conferenciaNombre;
        private String conferenciaDescripcion;
        private Integer dia;
        private LocalDate fechaReal;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private String escenario;
        private String primerConferencistaNombre;
        private String primerConferencistaFoto;
    }
}
