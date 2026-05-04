package com.example.back.service;

// 📁 src/main/java/com/example/back/service/InscripcionService.java

import com.example.back.dto.InscripcionDTO;
import com.example.back.model.*;
import com.example.back.repository.ConferenciaRepository;
import com.example.back.repository.InscripcionRepository;
import com.example.back.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final ConferenciaRepository conferenciaRepository;
    private final UsuarioRepository usuarioRepository;

    // ── Inscribir estudiante a una conferencia ────────────────────────────────
    @Transactional
    public InscripcionDTO.Response inscribir(Long estudianteId, InscripcionDTO.Request request) {
        Usuario estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante no encontrado"));

        Conferencia conferencia = conferenciaRepository.findById(request.getConferenciaId())
                .orElseThrow(() -> new EntityNotFoundException("Conferencia no encontrada"));

        // Validación 1: que sea estudiante
        if (estudiante.getRol() != Usuario.Rol.ESTUDIANTE) {
            throw new RuntimeException("Solo los estudiantes pueden inscribirse a conferencias");
        }

        // Validación 2: no inscrito ya
        if (inscripcionRepository.existsByEstudianteIdAndConferenciaId(
                estudianteId, request.getConferenciaId())) {
            throw new RuntimeException("Ya estás inscrito en esta conferencia");
        }

        // Validación 3: hay cupo disponible
        if (conferencia.getCupoDisponible() <= 0) {
            throw new RuntimeException("Esta conferencia no tiene cupo disponible");
        }

        // Validación 4: no hay cruce de horario
        boolean cruce = conferenciaRepository.existeCruceDeHorario(
                estudianteId,
                conferencia.getSemanaAcademica().getId(),
                conferencia.getDia(),
                conferencia.getId(),
                conferencia.getHoraInicio(),
                conferencia.getHoraFin()
        );
        if (cruce) {
            throw new RuntimeException(
                    "Ya tienes otra conferencia registrada en ese horario el día "
                    + conferencia.getDia());
        }

        // Descontar cupo
        conferencia.setCupoDisponible(conferencia.getCupoDisponible() - 1);
        conferenciaRepository.save(conferencia);

        // Crear inscripción
        Inscripcion inscripcion = Inscripcion.builder()
                .estudiante(estudiante)
                .conferencia(conferencia)
                .build();

        return toResponse(inscripcionRepository.save(inscripcion));
    }

    // ── Cancelar inscripción ──────────────────────────────────────────────────
    @Transactional
    public void cancelar(Long estudianteId, Long conferenciaId) {
        Inscripcion inscripcion = inscripcionRepository
                .findByEstudianteIdAndConferenciaId(estudianteId, conferenciaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró inscripción para este estudiante en esta conferencia"));

        // Devolver el cupo
        Conferencia conferencia = inscripcion.getConferencia();
        conferencia.setCupoDisponible(conferencia.getCupoDisponible() + 1);
        conferenciaRepository.save(conferencia);

        inscripcionRepository.delete(inscripcion);
    }

    // ── Agenda personal del estudiante ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<InscripcionDTO.AgendaItem> obtenerAgenda(Long estudianteId, Long semanaId) {
        return inscripcionRepository.findAgendaEstudiante(estudianteId, semanaId)
                .stream()
                .map(this::toAgendaItem)
                .collect(Collectors.toList());
    }

    // ── Listar inscripciones de una conferencia (para el admin) ───────────────
    @Transactional(readOnly = true)
    public List<InscripcionDTO.Response> listarPorConferencia(Long conferenciaId) {
        return inscripcionRepository.findByConferenciaId(conferenciaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private InscripcionDTO.Response toResponse(Inscripcion i) {
        Conferencia c = i.getConferencia();
        return InscripcionDTO.Response.builder()
                .id(i.getId())
                .estudianteId(i.getEstudiante().getId())
                .estudianteNombre(i.getEstudiante().getNombre())
                .conferenciaId(c.getId())
                .conferenciaNombre(c.getNombre())
                .conferenciaDia(c.getDia())
                .conferenciaFechaReal(c.getSemanaAcademica().getFechaDelDia(c.getDia()))
                .conferenciaHoraInicio(c.getHoraInicio())
                .conferenciaHoraFin(c.getHoraFin())
                .fechaInscripcion(i.getFechaInscripcion())
                .build();
    }

    private InscripcionDTO.AgendaItem toAgendaItem(Inscripcion i) {
        Conferencia c = i.getConferencia();
        String primerNombre = null;
        String primerFoto = null;
        if (!c.getConferencistas().isEmpty()) {
            primerNombre = c.getConferencistas().get(0).getNombre();
            primerFoto = c.getConferencistas().get(0).getFotografiaUrl();
        }
        return InscripcionDTO.AgendaItem.builder()
                .inscripcionId(i.getId())
                .conferenciaId(c.getId())
                .conferenciaNombre(c.getNombre())
                .conferenciaDescripcion(c.getDescripcion())
                .dia(c.getDia())
                .fechaReal(c.getSemanaAcademica().getFechaDelDia(c.getDia()))
                .horaInicio(c.getHoraInicio())
                .horaFin(c.getHoraFin())
                .escenario(c.getEscenario().name())
                .primerConferencistaNombre(primerNombre)
                .primerConferencistaFoto(primerFoto)
                .build();
    }
}
