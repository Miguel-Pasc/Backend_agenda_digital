package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/ConferenciaDTO.java

import com.example.back.model.Carrera;
import com.example.back.model.Conferencia.Escenario;
import com.example.back.model.Conferencia.TipoConferencia;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ConferenciaDTO {

    // ── Crear conferencia (solo admin) ────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CrearRequest {
        @NotNull(message = "El día es requerido")
        @Min(value = 1, message = "El día debe ser al menos 1")
        private Integer dia;

        @NotNull(message = "La hora de inicio es requerida")
        private LocalTime horaInicio;

        @NotNull(message = "La hora de fin es requerida")
        private LocalTime horaFin;

        @NotBlank(message = "El nombre es requerido")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        private String nombre;

        private String descripcion;

        @NotNull(message = "El tipo es requerido")
        private TipoConferencia tipo;

        @NotNull(message = "El escenario es requerido")
        private Escenario escenario;

        @NotNull(message = "El cupo es requerido")
        @Min(value = 1, message = "El cupo debe ser al menos 1")
        private Integer cupo;

        @NotNull(message = "La carrera es requerida")
        private Carrera carrera;    // TODAS, SISTEMAS, INNOVACION, CONTADURIA

        private String logoUrl;

        // Lista de conferencistas que se crean junto con la conferencia
        private List<ConferencistaDTO.Request> conferencistas;
    }

    // ── Actualizar conferencia ────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActualizarRequest {
        private Integer dia;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private String nombre;
        private String descripcion;
        private TipoConferencia tipo;
        private Escenario escenario;
        private Integer cupo;
        private Carrera carrera;
        private String logoUrl;
        private List<ConferencistaDTO.Request> conferencistas; // ← AGREGAR ESTA LÍNEA
    }

    // ── Respuesta completa (incluye conferencistas y fecha real calculada) ─────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Integer dia;
        private LocalDate fechaReal;        // calculada: fechaInicio semana + (dia-1)
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private String nombre;
        private String descripcion;
        private TipoConferencia tipo;
        private Escenario escenario;
        private Integer cupo;
        private Integer cupoDisponible;
        private Carrera carrera;
        private String logoUrl;
        private Long semanaAcademicaId;
        private List<ConferencistaDTO.Response> conferencistas;
        private Boolean inscrito;           // true si el usuario autenticado está inscrito
    }

    // ── Respuesta resumida para tarjetas (sin conferencistas completos) ────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResumenResponse {
        private Long id;
        private Integer dia;
        private LocalDate fechaReal;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private String nombre;
        private String descripcion;          // ← agregar
        private TipoConferencia tipo;
        private Escenario escenario;
        private Integer cupoDisponible;
        private Carrera carrera;
        private String logoUrl;              // ← agregar
        private String primerConferencistaNombre;
        private String primerConferencistaFoto;
        private String primerConferencistaPerfil;  // ← agregar
        private String primerConferencistaBio;     // ← agregar
        private Boolean inscrito;
    }

    // ── Filtros para consultar conferencias ───────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FiltroRequest {
        private Integer dia;            // null = todos los días
        private Carrera carrera;        // null = todas las carreras
        private String busqueda;        // null = sin búsqueda por texto
    }
}
