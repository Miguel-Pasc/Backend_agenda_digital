package com.example.back.repository;

// 📁 src/main/java/com/example/back/repository/ConferenciaRepository.java

import com.example.back.model.Carrera;
import com.example.back.model.Conferencia;
import com.example.back.model.Conferencia.TipoConferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConferenciaRepository extends JpaRepository<Conferencia, Long> {

    // Todas las conferencias de una semana académica
    List<Conferencia> findBySemanaAcademicaIdOrderByDiaAscHoraInicioAsc(Long semanaId);

    // Conferencias de un día específico dentro de una semana
    List<Conferencia> findBySemanaAcademicaIdAndDiaOrderByHoraInicioAsc(Long semanaId, Integer dia);

    // Filtrar por carrera dentro de una semana
    // Incluye las de carrera TODAS y las de la carrera específica
    @Query("""
        SELECT c FROM Conferencia c
        WHERE c.semanaAcademica.id = :semanaId
        AND (c.carrera = 'TODAS' OR c.carrera = :carrera)
        ORDER BY c.dia ASC, c.horaInicio ASC
    """)
    List<Conferencia> findBySemanaAndCarrera(
            @Param("semanaId") Long semanaId,
            @Param("carrera") Carrera carrera
    );

    // Filtrar por día y carrera
    @Query("""
        SELECT c FROM Conferencia c
        WHERE c.semanaAcademica.id = :semanaId
        AND c.dia = :dia
        AND (c.carrera = 'TODAS' OR c.carrera = :carrera)
        ORDER BY c.horaInicio ASC
    """)
    List<Conferencia> findBySemanaAndDiaAndCarrera(
            @Param("semanaId") Long semanaId,
            @Param("dia") Integer dia,
            @Param("carrera") Carrera carrera
    );

    // Buscar por nombre de conferencia (para el buscador)
    @Query("""
        SELECT c FROM Conferencia c
        WHERE c.semanaAcademica.id = :semanaId
        AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
        ORDER BY c.dia ASC, c.horaInicio ASC
    """)
    List<Conferencia> findBySemanaAndNombreContaining(
            @Param("semanaId") Long semanaId,
            @Param("nombre") String nombre
    );

    // Buscar por nombre de conferencista (para el buscador)
    @Query("""
        SELECT DISTINCT c FROM Conferencia c
        JOIN c.conferencistas conf
        WHERE c.semanaAcademica.id = :semanaId
        AND LOWER(conf.nombre) LIKE LOWER(CONCAT('%', :nombreConferencista, '%'))
        ORDER BY c.dia ASC, c.horaInicio ASC
    """)
    List<Conferencia> findBySemanaAndNombreConferencista(
            @Param("semanaId") Long semanaId,
            @Param("nombreConferencista") String nombreConferencista
    );

    // Verificar si existe un cruce de horario para un estudiante en un día
    // Usado antes de inscribir al estudiante
    @Query("""
        SELECT COUNT(i) > 0 FROM Inscripcion i
        WHERE i.estudiante.id = :estudianteId
        AND i.conferencia.semanaAcademica.id = :semanaId
        AND i.conferencia.dia = :dia
        AND i.conferencia.id <> :conferenciaId
        AND i.conferencia.horaInicio < :horaFin
        AND i.conferencia.horaFin > :horaInicio
    """)
    boolean existeCruceDeHorario(
            @Param("estudianteId") Long estudianteId,
            @Param("semanaId") Long semanaId,
            @Param("dia") Integer dia,
            @Param("conferenciaId") Long conferenciaId,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    // Buscar por nombre de conferencia filtrando también por día
    @Query("""
    SELECT c FROM Conferencia c
    WHERE c.semanaAcademica.id = :semanaId
    AND c.dia = :dia
    AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
    ORDER BY c.horaInicio ASC
""")
    List<Conferencia> findBySemanaAndDiaAndNombreContaining(
            @Param("semanaId") Long semanaId,
            @Param("dia") Integer dia,
            @Param("nombre") String nombre
    );

    // Buscar por nombre de conferencista filtrando también por día
    @Query("""
    SELECT DISTINCT c FROM Conferencia c
    JOIN c.conferencistas conf
    WHERE c.semanaAcademica.id = :semanaId
    AND c.dia = :dia
    AND LOWER(conf.nombre) LIKE LOWER(CONCAT('%', :nombreConferencista, '%'))
    ORDER BY c.horaInicio ASC
""")
    List<Conferencia> findBySemanaAndDiaAndNombreConferencista(
            @Param("semanaId") Long semanaId,
            @Param("dia") Integer dia,
            @Param("nombreConferencista") String nombreConferencista
    );

    // Para el PDF: trae conferencias con sus conferencistas en una sola query
    @Query("""
    SELECT DISTINCT c FROM Conferencia c
    LEFT JOIN FETCH c.conferencistas
    WHERE c.semanaAcademica.id = :semanaId
    ORDER BY c.dia ASC, c.horaInicio ASC
""")
    List<Conferencia> findBySemanaConConferencistasParaPdf(
            @Param("semanaId") Long semanaId);


    // Agrega este método al final de tu ConferenciaRepository.java

    /**
     * Busca conferencias que tengan conflicto de horario en el mismo día y mismo escenario.
     * Excluye opcionalmente una conferencia (útil para edición).
     *
     * @param semanaId ID de la semana académica
     * @param dia Día de la conferencia
     * @param escenario Escenario de la conferencia
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @param idExcluir ID de conferencia a excluir (null si no aplicar)
     * @return Lista de conferencias conflictivas
     */
    @Query("""
    SELECT c FROM Conferencia c
    WHERE c.semanaAcademica.id = :semanaId
    AND c.dia = :dia
    AND c.escenario = :escenario
    AND (:idExcluir IS NULL OR c.id <> :idExcluir)
    AND c.horaInicio < :horaFin
    AND c.horaFin > :horaInicio
    ORDER BY c.horaInicio ASC
""")
    List<Conferencia> findConflictosHorario(
            @Param("semanaId") Long semanaId,
            @Param("dia") Integer dia,
            @Param("escenario") Conferencia.Escenario escenario,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("idExcluir") Long idExcluir
    );
}