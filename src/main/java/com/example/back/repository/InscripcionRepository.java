package com.example.back.repository;

// 📁 src/main/java/com/example/back/repository/InscripcionRepository.java

import com.example.back.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    // Todas las inscripciones de un estudiante
    List<Inscripcion> findByEstudianteId(Long estudianteId);

    // Todas las inscripciones de un estudiante en una semana (agenda personal)
    @Query("""
        SELECT i FROM Inscripcion i
        WHERE i.estudiante.id = :estudianteId
        AND i.conferencia.semanaAcademica.id = :semanaId
        ORDER BY i.conferencia.dia ASC, i.conferencia.horaInicio ASC
    """)
    List<Inscripcion> findAgendaEstudiante(
            @Param("estudianteId") Long estudianteId,
            @Param("semanaId") Long semanaId
    );

    // Verificar si un estudiante ya está inscrito en una conferencia
    boolean existsByEstudianteIdAndConferenciaId(Long estudianteId, Long conferenciaId);

    // Obtener inscripción específica para poder cancelarla
    Optional<Inscripcion> findByEstudianteIdAndConferenciaId(Long estudianteId, Long conferenciaId);

    // Contar inscripciones de una conferencia (para reportes)
    long countByConferenciaId(Long conferenciaId);

    // Todas las inscripciones de una conferencia (para reporte del admin)
    List<Inscripcion> findByConferenciaId(Long conferenciaId);
}