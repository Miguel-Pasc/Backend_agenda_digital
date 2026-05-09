package com.example.back.service;

// 📁 src/main/java/com/example/back/service/SemanaAcademicaService.java

import com.example.back.dto.SemanaAcademicaDTO;
import com.example.back.model.SemanaAcademica;
import com.example.back.repository.SemanaAcademicaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemanaAcademicaService {

    private final SemanaAcademicaRepository semanaRepository;

    // ── Crear nueva semana académica ──────────────────────────────────────────
    @Transactional
    public SemanaAcademicaDTO.Response crear(SemanaAcademicaDTO.CrearRequest request) {
        // ← Cambiar validación: ahora un año solo puede usarse una vez
        if (semanaRepository.existsByAnio(request.getAnio())) {
            throw new RuntimeException("Ya existe una semana académica para el año " + request.getAnio());
        }
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        SemanaAcademica semana = SemanaAcademica.builder()
                .numero(request.getNumero())
                .anio(request.getAnio())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .activa(false)
                .build();

        return toResponse(semanaRepository.save(semana));
    }

    @Transactional
    public SemanaAcademicaDTO.Response actualizar(Long id, SemanaAcademicaDTO.ActualizarRequest request) {
        SemanaAcademica semana = buscarPorId(id);

        // Si cambia el año, verificar que el nuevo año no esté ocupado por otra semana
        if (!semana.getAnio().equals(request.getAnio()) &&
                semanaRepository.existsByAnio(request.getAnio())) {
            throw new RuntimeException("Ya existe una semana académica para el año " + request.getAnio());
        }
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        semana.setNumero(request.getNumero());
        semana.setAnio(request.getAnio());
        semana.setFechaInicio(request.getFechaInicio());
        semana.setFechaFin(request.getFechaFin());

        return toResponse(semanaRepository.save(semana));
    }

    // ── Obtener semana activa ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public SemanaAcademicaDTO.Response obtenerActiva() {
        return toResponse(semanaRepository.findByActivaTrue()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No hay ninguna semana académica activa actualmente")));
    }

    // ── Obtener por ID ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public SemanaAcademicaDTO.Response obtenerPorId(Long id) {
        return toResponse(buscarPorId(id));
    }

    // ── Listar todas (historial) ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SemanaAcademicaDTO.ResumenResponse> listarTodas() {
        return semanaRepository.findAllByOrderByAnioDescNumeroDesc()
                .stream().map(this::toResumen).collect(Collectors.toList());
    }

    // ── Listar por año ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SemanaAcademicaDTO.ResumenResponse> listarPorAnio(Integer anio) {
        return semanaRepository.findByAnioOrderByNumeroDesc(anio)
                .stream().map(this::toResumen).collect(Collectors.toList());
    }

    // ── Activar semana (desactiva todas las demás primero) ────────────────────
    @Transactional
    public SemanaAcademicaDTO.Response activar(Long id) {
        SemanaAcademica semana = buscarPorId(id);
        semanaRepository.desactivarTodas();     // desactiva cualquier semana activa
        semana.setActiva(true);
        return toResponse(semanaRepository.save(semana));
    }

    // ── Desactivar semana ─────────────────────────────────────────────────────
    @Transactional
    public SemanaAcademicaDTO.Response desactivar(Long id) {
        SemanaAcademica semana = buscarPorId(id);
        semana.setActiva(false);
        return toResponse(semanaRepository.save(semana));
    }

    // ── Eliminar semana (solo si no tiene conferencias) ───────────────────────
    @Transactional
    public void eliminar(Long id) {
        SemanaAcademica semana = buscarPorId(id);
        if (!semana.getConferencias().isEmpty()) {
            throw new RuntimeException(
                    "No se puede eliminar una semana académica que tiene conferencias registradas");
        }
        semanaRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    public SemanaAcademica buscarPorId(Long id) {
        return semanaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Semana académica no encontrada con id: " + id));
    }

    public SemanaAcademicaDTO.Response toResponse(SemanaAcademica s) {
        return SemanaAcademicaDTO.Response.builder()
                .id(s.getId())
                .numero(s.getNumero())
                .anio(s.getAnio())
                .fechaInicio(s.getFechaInicio())
                .fechaFin(s.getFechaFin())
                .activa(s.getActiva())
                .duracionDias(s.getDuracionDias())
                .totalConferencias(s.getConferencias().size())
                .build();
    }

    private SemanaAcademicaDTO.ResumenResponse toResumen(SemanaAcademica s) {
        return SemanaAcademicaDTO.ResumenResponse.builder()
                .id(s.getId())
                .numero(s.getNumero())
                .anio(s.getAnio())
                .fechaInicio(s.getFechaInicio())
                .fechaFin(s.getFechaFin())
                .activa(s.getActiva())
                .build();
    }
}
