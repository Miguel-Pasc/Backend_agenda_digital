package com.example.back.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semanas_academicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemanaAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;         // Ej: 15

    @Column(nullable = false)
    private Integer anio;           // Ej: 2026

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = false;

    @OneToMany(mappedBy = "semanaAcademica", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Conferencia> conferencias = new ArrayList<>();

    @Column(name = "logo_estado_url")
    private String logoEstadoUrl;

    @Column(name = "logo_jornada_url")
    private String logoJornadaUrl;

    @Column(name = "logo_ues_url")
    private String logoUesUrl;

    @Column(name = "frase_pie", length = 500)
    private String frasePie;

    // ── Método utilitario ─────────────────────────────────────────────────────
    // Calcula cuántos días dura la semana académica
    public int getDuracionDias() {
        return (int) (fechaFin.toEpochDay() - fechaInicio.toEpochDay()) + 1;
    }

    // Calcula la fecha real de un día (dia=1 → fechaInicio, dia=2 → fechaInicio+1, etc.)
    public LocalDate getFechaDelDia(Integer dia) {
        return this.fechaInicio.plusDays(dia - 1);
    }
}
