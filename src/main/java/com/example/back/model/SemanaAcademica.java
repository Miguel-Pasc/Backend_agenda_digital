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

    // ── Método utilitario ─────────────────────────────────────────────────────
    // Calcula cuántos días dura la semana académica
    public int getDuracionDias() {
        return (int) (fechaFin.toEpochDay() - fechaInicio.toEpochDay()) + 1;
    }

    // Calcula la fecha real de un día (dia=1 → fechaInicio, dia=2 → fechaInicio+1, etc.)
    public LocalDate getFechaDelDia(int dia) {
        return fechaInicio.plusDays(dia - 1);
    }
}
