package com.example.back.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conferencistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conferencista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "perfil_profesional", length = 200)
    private String perfilProfesional;

    @Column(columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "fotografia_url", length = 500)
    private String fotografiaUrl;       // nullable — se puede llenar después

    @Column(name = "logo_url", length = 500)
    private String logoUrl;             // nullable — se puede llenar después

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conferencia_id", nullable = false)
    private Conferencia conferencia;
}
