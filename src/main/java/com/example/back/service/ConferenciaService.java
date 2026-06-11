package com.example.back.service;

// 📁 src/main/java/com/example/back/service/ConferenciaService.java

import com.example.back.dto.ConferenciaDTO;
import com.example.back.dto.ConferencistaDTO;
import com.example.back.exception.ConflictoHorarioException;
import com.example.back.model.*;
import com.example.back.repository.ConferenciaRepository;
import com.example.back.repository.InscripcionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConferenciaService {

    private final ConferenciaRepository conferenciaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final SemanaAcademicaService semanaService;

    // ── Crear conferencia ─────────────────────────────────────────────────────
    @Transactional
    public ConferenciaDTO.Response crear(Long semanaId, ConferenciaDTO.CrearRequest request) {
        SemanaAcademica semana = semanaService.buscarPorId(semanaId);

        // Validar que el día esté dentro del rango de la semana
        if (request.getDia() < 1 || request.getDia() > semana.getDuracionDias()) {
            throw new RuntimeException("El día " + request.getDia()
                    + " no existe en esta semana académica (duración: "
                    + semana.getDuracionDias() + " días)");
        }
        // Validar que hora fin sea posterior a hora inicio
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // ✅ NUEVA VALIDACIÓN: Verificar conflictos de horario
        validarSinConflictosHorario(
                semanaId,
                request.getDia(),
                request.getEscenario(),
                request.getHoraInicio(),
                request.getHoraFin(),
                null  // idExcluir = null porque es creación
        );

        // Validar cupo máximo según escenario
        validarCupoPorEscenario(request.getEscenario(), request.getCupo());

        Conferencia conferencia = Conferencia.builder()
                .dia(request.getDia())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipo(request.getTipo())
                .escenario(request.getEscenario())
                .cupo(request.getCupo())
                .carrera(request.getCarrera())
                .logoUrl(request.getLogoUrl())
                .semanaAcademica(semana)
                .build();

        // Agregar conferencistas
        if (request.getConferencistas() != null && !request.getConferencistas().isEmpty()) {
            List<Conferencista> conferencistas = request.getConferencistas().stream()
                    .map(c -> Conferencista.builder()
                            .nombre(c.getNombre())
                            .perfilProfesional(c.getPerfilProfesional())
                            .biografia(c.getBiografia())
                            .fotografiaUrl(c.getFotografiaUrl())
                            .logoUrl(c.getLogoUrl())
                            .conferencia(conferencia)
                            .build())
                    .collect(Collectors.toList());
            conferencia.setConferencistas(conferencistas);
        }

        return toResponse(conferenciaRepository.save(conferencia), null);
    }

    // ── Listar con filtros (para todos los usuarios) ──────────────────────────
    @Transactional(readOnly = true)
    public List<ConferenciaDTO.ResumenResponse> listarConFiltros(
            Long semanaId, ConferenciaDTO.FiltroRequest filtros, Long estudianteId) {

        List<Conferencia> lista;

        // Aplicar filtros combinados
        if (filtros.getBusqueda() != null && !filtros.getBusqueda().isBlank()) {
            List<Conferencia> porNombre = filtros.getDia() != null
                    ? conferenciaRepository.findBySemanaAndDiaAndNombreContaining(
                    semanaId, filtros.getDia(), filtros.getBusqueda())
                    : conferenciaRepository.findBySemanaAndNombreContaining(
                    semanaId, filtros.getBusqueda());

            List<Conferencia> porConferencista = filtros.getDia() != null
                    ? conferenciaRepository.findBySemanaAndDiaAndNombreConferencista(
                    semanaId, filtros.getDia(), filtros.getBusqueda())
                    : conferenciaRepository.findBySemanaAndNombreConferencista(
                    semanaId, filtros.getBusqueda());

            Set<Long> ids = porNombre.stream().map(Conferencia::getId).collect(Collectors.toSet());
            porConferencista.forEach(c -> { if (!ids.contains(c.getId())) porNombre.add(c); });
            lista = porNombre;
        } else if (filtros.getDia() != null && filtros.getCarrera() != null) {
            lista = conferenciaRepository.findBySemanaAndDiaAndCarrera(
                    semanaId, filtros.getDia(), filtros.getCarrera());
        } else if (filtros.getDia() != null) {
            lista = conferenciaRepository
                    .findBySemanaAcademicaIdAndDiaOrderByHoraInicioAsc(semanaId, filtros.getDia());
        } else if (filtros.getCarrera() != null) {
            lista = conferenciaRepository
                    .findBySemanaAndCarrera(semanaId, filtros.getCarrera());
        } else {
            lista = conferenciaRepository
                    .findBySemanaAcademicaIdOrderByDiaAscHoraInicioAsc(semanaId);
        }

        // Obtener IDs de conferencias en las que está inscrito el estudiante
        Set<Long> inscritas = obtenerInscritasEstudiante(estudianteId, semanaId);

        return lista.stream()
                .map(c -> toResumen(c, inscritas.contains(c.getId())))
                .collect(Collectors.toList());
    }

    // ── Obtener por ID ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ConferenciaDTO.Response obtenerPorId(Long id, Long estudianteId) {
        Conferencia c = buscarPorId(id);
        boolean inscrito = estudianteId != null &&
                inscripcionRepository.existsByEstudianteIdAndConferenciaId(estudianteId, id);
        return toResponse(c, inscrito);
    }

    // ── Actualizar conferencia ────────────────────────────────────────────────
    @Transactional
    public ConferenciaDTO.Response actualizar(Long id, ConferenciaDTO.ActualizarRequest request) {
        Conferencia conferencia = buscarPorId(id);

        // Determinar valores finales (si vienen en request o se mantienen)
        Integer nuevoDia = request.getDia() != null ? request.getDia() : conferencia.getDia();
        LocalTime nuevaHoraInicio = request.getHoraInicio() != null ? request.getHoraInicio() : conferencia.getHoraInicio();
        LocalTime nuevaHoraFin = request.getHoraFin() != null ? request.getHoraFin() : conferencia.getHoraFin();
        Conferencia.Escenario nuevoEscenario = request.getEscenario() != null ? request.getEscenario() : conferencia.getEscenario();

        // Validar que hora fin sea posterior a hora inicio (si alguna hora cambió)
        if ((request.getHoraInicio() != null || request.getHoraFin() != null)
                && !nuevaHoraFin.isAfter(nuevaHoraInicio)) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar día si cambió
        if (request.getDia() != null) {
            int duracion = conferencia.getSemanaAcademica().getDuracionDias();
            if (request.getDia() < 1 || request.getDia() > duracion) {
                throw new RuntimeException("El día " + request.getDia()
                        + " no existe en esta semana académica (duración: "
                        + duracion + " días)");
            }
            conferencia.setDia(request.getDia());
        }

        // ✅ VALIDACIÓN DE CONFLICTOS DE HORARIO
        validarSinConflictosHorario(
                conferencia.getSemanaAcademica().getId(),
                nuevoDia,
                nuevoEscenario,
                nuevaHoraInicio,
                nuevaHoraFin,
                id  // Excluir la propia conferencia
        );

        // Validar cupo máximo según escenario (si cambió escenario o cupo)
        int cupoAValidar = request.getCupo() != null ? request.getCupo() : conferencia.getCupo();
        validarCupoPorEscenario(nuevoEscenario, cupoAValidar);

        // Aplicar cambios
        if (request.getHoraInicio() != null) conferencia.setHoraInicio(request.getHoraInicio());
        if (request.getHoraFin() != null)    conferencia.setHoraFin(request.getHoraFin());
        if (request.getNombre() != null)     conferencia.setNombre(request.getNombre());
        if (request.getDescripcion() != null) conferencia.setDescripcion(request.getDescripcion());
        if (request.getTipo() != null)       conferencia.setTipo(request.getTipo());
        if (request.getEscenario() != null)  conferencia.setEscenario(request.getEscenario());
        if (request.getCarrera() != null)    conferencia.setCarrera(request.getCarrera());
        if (request.getLogoUrl() != null)    conferencia.setLogoUrl(request.getLogoUrl());

        if (request.getCupo() != null) {
            int diferencia = request.getCupo() - conferencia.getCupo();
            conferencia.setCupo(request.getCupo());
            conferencia.setCupoDisponible(conferencia.getCupoDisponible() + diferencia);
        }

        // ── Actualizar conferencistas si vienen en el request ─────────────────
        if (request.getConferencistas() != null && !request.getConferencistas().isEmpty()) {
            List<Conferencista> existentes = conferencia.getConferencistas();

            for (int i = 0; i < request.getConferencistas().size(); i++) {
                ConferencistaDTO.Request confReq = request.getConferencistas().get(i);

                if (i < existentes.size()) {
                    Conferencista conf = existentes.get(i);
                    if (confReq.getNombre() != null)            conf.setNombre(confReq.getNombre());
                    if (confReq.getPerfilProfesional() != null) conf.setPerfilProfesional(confReq.getPerfilProfesional());
                    if (confReq.getBiografia() != null)         conf.setBiografia(confReq.getBiografia());
                    if (confReq.getFotografiaUrl() != null)     conf.setFotografiaUrl(confReq.getFotografiaUrl());
                    if (confReq.getLogoUrl() != null)           conf.setLogoUrl(confReq.getLogoUrl());
                } else {
                    Conferencista nuevo = Conferencista.builder()
                            .nombre(confReq.getNombre())
                            .perfilProfesional(confReq.getPerfilProfesional())
                            .biografia(confReq.getBiografia())
                            .fotografiaUrl(confReq.getFotografiaUrl())
                            .logoUrl(confReq.getLogoUrl())
                            .conferencia(conferencia)
                            .build();
                    existentes.add(nuevo);
                }
            }
        }

        return toResponse(conferenciaRepository.save(conferencia), null);
    }

    // ── Eliminar conferencia ──────────────────────────────────────────────────
    @Transactional
    public void eliminar(Long id) {
        if (!conferenciaRepository.existsById(id)) {
            throw new EntityNotFoundException("Conferencia no encontrada con id: " + id);
        }
        conferenciaRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    public Conferencia buscarPorId(Long id) {
        return conferenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Conferencia no encontrada con id: " + id));
    }

    private void validarCupoPorEscenario(Conferencia.Escenario escenario, int cupo) {
        int maximo = switch (escenario) {
            case AULA_MAGNA       -> 100;
            case ZONA_DE_CULTIVOS -> 150;
            case SALA_DE_COMPUTO  -> 20;
        };
        if (cupo > maximo) {
            throw new RuntimeException(
                "El escenario \"" + escenario.name() + "\" tiene una capacidad máxima de "
                + maximo + " personas. El cupo ingresado (" + cupo + ") lo excede.");
        }
        if (cupo < 1) {
            throw new RuntimeException("El cupo debe ser al menos 1");
        }
    }

    private Set<Long> obtenerInscritasEstudiante(Long estudianteId, Long semanaId) {
        if (estudianteId == null) return Set.of();
        return inscripcionRepository.findAgendaEstudiante(estudianteId, semanaId)
                .stream()
                .map(i -> i.getConferencia().getId())
                .collect(Collectors.toSet());
    }

    public ConferenciaDTO.Response toResponse(Conferencia c, Boolean inscrito) {
        List<ConferencistaDTO.Response> conferencistas = c.getConferencistas().stream()
                .map(conf -> ConferencistaDTO.Response.builder()
                        .id(conf.getId())
                        .nombre(conf.getNombre())
                        .perfilProfesional(conf.getPerfilProfesional())
                        .biografia(conf.getBiografia())
                        .fotografiaUrl(conf.getFotografiaUrl())
                        .logoUrl(conf.getLogoUrl())
                        .build())
                .collect(Collectors.toList());

        return ConferenciaDTO.Response.builder()
                .id(c.getId())
                .dia(c.getDia())
                .fechaReal(c.getSemanaAcademica().getFechaDelDia(c.getDia()))
                .horaInicio(c.getHoraInicio())
                .horaFin(c.getHoraFin())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .tipo(c.getTipo())
                .escenario(c.getEscenario())
                .cupo(c.getCupo())
                .cupoDisponible(c.getCupoDisponible())
                .carrera(c.getCarrera())
                .logoUrl(c.getLogoUrl())
                .semanaAcademicaId(c.getSemanaAcademica().getId())
                .conferencistas(conferencistas)
                .inscrito(inscrito)
                .build();
    }

    private ConferenciaDTO.ResumenResponse toResumen(Conferencia c, Boolean inscrito) {
        String primerNombre  = null;
        String primerFoto    = null;
        String primerPerfil  = null;  // ← agregar
        String primerBio     = null;  // ← agregar

        if (!c.getConferencistas().isEmpty()) {
            Conferencista primero = c.getConferencistas().get(0);
            primerNombre = primero.getNombre();
            primerFoto   = primero.getFotografiaUrl();
            primerPerfil = primero.getPerfilProfesional();  // ← agregar
            primerBio    = primero.getBiografia();           // ← agregar
        }

        return ConferenciaDTO.ResumenResponse.builder()
                .id(c.getId())
                .dia(c.getDia())
                .fechaReal(c.getSemanaAcademica().getFechaDelDia(c.getDia()))
                .horaInicio(c.getHoraInicio())
                .horaFin(c.getHoraFin())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())                    // ← agregar
                .tipo(c.getTipo())
                .escenario(c.getEscenario())
                .cupoDisponible(c.getCupoDisponible())
                .carrera(c.getCarrera())
                .logoUrl(c.getLogoUrl())                            // ← agregar
                .primerConferencistaNombre(primerNombre)
                .primerConferencistaFoto(primerFoto)
                .primerConferencistaPerfil(primerPerfil)            // ← agregar
                .primerConferencistaBio(primerBio)                  // ← agregar
                .inscrito(inscrito)
                .build();
    }


    // Agrega este método en ConferenciaService.java

    /**
     * Valida que no existan conflictos de horario en el mismo día y mismo escenario.
     * Lanza excepción si encuentra un conflicto.
     *
     * @param semanaId ID de la semana académica
     * @param dia Día de la conferencia
     * @param escenario Escenario
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @param idExcluir ID a excluir (para edición, null si es creación)
     * @throws ConflictoHorarioException si hay conflicto
     */
    private void validarSinConflictosHorario(Long semanaId, Integer dia,
                                             Conferencia.Escenario escenario,
                                             LocalTime horaInicio, LocalTime horaFin,
                                             Long idExcluir) {

        List<Conferencia> conflictos = conferenciaRepository.findConflictosHorario(
                semanaId, dia, escenario, horaInicio, horaFin, idExcluir
        );

        if (!conflictos.isEmpty()) {
            Conferencia conflicto = conflictos.get(0);
            throw new ConflictoHorarioException(
                    String.format("❌ Conflicto de horario: Ya existe una conferencia en el escenario '%s' el día %d de %s a %s",
                            getEscenarioNombre(escenario),
                            dia,
                            conflicto.getHoraInicio().toString(),
                            conflicto.getHoraFin().toString()
                    ),
                    conflicto.getId(),
                    conflicto.getNombre(),
                    conflicto.getHoraInicio(),
                    conflicto.getHoraFin()
            );
        }
    }

    /**
     * Helper para obtener nombre legible del escenario
     */
    private String getEscenarioNombre(Conferencia.Escenario escenario) {
        return switch (escenario) {
            case AULA_MAGNA -> "Aula Magna";
            case SALA_DE_COMPUTO -> "Sala de Cómputo";
            case ZONA_DE_CULTIVOS -> "Zona de Cultivos";
        };
    }
}
