package com.example.back.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conferencias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conferencia {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer dia;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoConferencia tipo;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Escenario escenario;

    @Column(nullable = false)
    private Integer cupo;

    @Column(name = "cupo_disponible", nullable = false)
    private Integer cupoDisponible;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    @Builder.Default
    private Carrera carrera = Carrera.TODAS;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semana_academica_id", nullable = false)
    private SemanaAcademica semanaAcademica;

    @OneToMany(mappedBy = "conferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Conferencista> conferencistas = new ArrayList<>();

    @OneToMany(mappedBy = "conferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @PrePersist
    public void prePersist() { this.cupoDisponible = this.cupo; }

    public enum TipoConferencia { INAUGURACION, TALLER, CONFERENCIA }
    public enum Escenario { AULA_MAGNA, SALA_DE_COMPUTO, ZONA_DE_CULTIVOS }
}