package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/SemanaAcademicaController.java

import com.example.back.dto.ConferenciaDTO;
import com.example.back.dto.SemanaAcademicaDTO;
import com.example.back.model.Carrera;
import com.example.back.service.ConferenciaService;
import com.example.back.service.SemanaAcademicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semanas")
@RequiredArgsConstructor
public class SemanaAcademicaController {

    private final SemanaAcademicaService semanaService;
    private final ConferenciaService conferenciaService;

    // POST /api/semanas — solo ADMIN
    @PostMapping
    public ResponseEntity<SemanaAcademicaDTO.Response> crear(
            @Valid @RequestBody SemanaAcademicaDTO.CrearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(semanaService.crear(request));
    }

    // GET /api/semanas/activa — público
    // El frontend lo usa para saber qué semana mostrar
    @GetMapping("/activa")
    public ResponseEntity<SemanaAcademicaDTO.Response> obtenerActiva() {
        return ResponseEntity.ok(semanaService.obtenerActiva());
    }

    // GET /api/semanas — solo ADMIN (historial completo)
    @GetMapping
    public ResponseEntity<List<SemanaAcademicaDTO.ResumenResponse>> listarTodas(
            @RequestParam(required = false) Integer anio) {
        if (anio != null) {
            return ResponseEntity.ok(semanaService.listarPorAnio(anio));
        }
        return ResponseEntity.ok(semanaService.listarTodas());
    }

    // GET /api/semanas/{id} — solo ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<SemanaAcademicaDTO.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(semanaService.obtenerPorId(id));
    }

    // PUT /api/semanas/{id}/activar — solo ADMIN
    // Activa esta semana y desactiva todas las demás
    @PutMapping("/{id}/activar")
    public ResponseEntity<SemanaAcademicaDTO.Response> activar(@PathVariable Long id) {
        return ResponseEntity.ok(semanaService.activar(id));
    }

    // PUT /api/semanas/{id}/desactivar — solo ADMIN
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<SemanaAcademicaDTO.Response> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(semanaService.desactivar(id));
    }

    // DELETE /api/semanas/{id} — solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        semanaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/semanas/{id}/conferencias — público
    // Lista las conferencias de una semana con filtros opcionales
    // Usado por el invitado, el estudiante y el admin
    @GetMapping("/{id}/conferencias")
    public ResponseEntity<List<ConferenciaDTO.ResumenResponse>> listarConferencias(
            @PathVariable Long id,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Carrera carrera,
            @RequestParam(required = false) String busqueda) {

        ConferenciaDTO.FiltroRequest filtros = ConferenciaDTO.FiltroRequest.builder()
                .dia(dia)
                .carrera(carrera)
                .busqueda(busqueda)
                .build();

        // estudianteId = null porque es ruta pública (sin autenticación requerida)
        // Si el estudiante está autenticado, el frontend puede llamar al endpoint
        // /api/inscripciones/agenda para saber en cuáles está inscrito
        return ResponseEntity.ok(
                conferenciaService.listarConFiltros(id, filtros, null));
    }
}
