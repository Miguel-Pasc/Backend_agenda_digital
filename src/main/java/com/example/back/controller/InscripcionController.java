package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/InscripcionController.java

import com.example.back.dto.InscripcionDTO;
import com.example.back.model.Usuario;
import com.example.back.repository.UsuarioRepository;
import com.example.back.service.InscripcionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final UsuarioRepository usuarioRepository;

    // POST /api/inscripciones — solo ESTUDIANTE
    // El estudiante se inscribe a una conferencia
    @PostMapping
    public ResponseEntity<InscripcionDTO.Response> inscribir(
            @RequestBody InscripcionDTO.Request request,
            Principal principal) {
        Long estudianteId = obtenerEstudianteId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscripcionService.inscribir(estudianteId, request));
    }

    // DELETE /api/inscripciones/{conferenciaId} — solo ESTUDIANTE
    // El estudiante cancela su inscripción a una conferencia
    @DeleteMapping("/{conferenciaId}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long conferenciaId,
            Principal principal) {
        Long estudianteId = obtenerEstudianteId(principal);
        inscripcionService.cancelar(estudianteId, conferenciaId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/inscripciones/agenda/{semanaId} — estudiante y admin
    // Devuelve la agenda personal del estudiante autenticado
    @GetMapping("/agenda/{semanaId}")
    public ResponseEntity<List<InscripcionDTO.AgendaItem>> obtenerAgenda(
            @PathVariable Long semanaId,
            Principal principal) {
        Long estudianteId = obtenerEstudianteId(principal);
        return ResponseEntity.ok(inscripcionService.obtenerAgenda(estudianteId, semanaId));
    }

    // GET /api/inscripciones/conferencia/{conferenciaId} — solo ADMIN
    // Lista todos los estudiantes inscritos en una conferencia
    @GetMapping("/conferencia/{conferenciaId}")
    public ResponseEntity<List<InscripcionDTO.Response>> listarPorConferencia(
            @PathVariable Long conferenciaId) {
        return ResponseEntity.ok(inscripcionService.listarPorConferencia(conferenciaId));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long obtenerEstudianteId(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No estás autenticado");
        }
        Usuario usuario = usuarioRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return usuario.getId();
    }
}
