package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/ConferenciaController.java

import com.example.back.dto.ConferenciaDTO;
import com.example.back.model.Usuario;
import com.example.back.repository.UsuarioRepository;
import com.example.back.service.ConferenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/conferencias")
@RequiredArgsConstructor
public class ConferenciaController {

    private final ConferenciaService conferenciaService;
    private final UsuarioRepository usuarioRepository;

    // POST /api/conferencias?semanaId={id} — solo ADMIN
    @PostMapping
    public ResponseEntity<ConferenciaDTO.Response> crear(
            @RequestParam Long semanaId,
            @Valid @RequestBody ConferenciaDTO.CrearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conferenciaService.crear(semanaId, request));
    }

    // GET /api/conferencias/{id} — público
    // Si el usuario está autenticado como estudiante, devuelve si está inscrito
    @GetMapping("/{id}")
    public ResponseEntity<ConferenciaDTO.Response> obtenerPorId(
            @PathVariable Long id,
            Principal principal) {
        Long estudianteId = obtenerEstudianteId(principal);
        return ResponseEntity.ok(conferenciaService.obtenerPorId(id, estudianteId));
    }

    // PUT /api/conferencias/{id} — solo ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<ConferenciaDTO.Response> actualizar(
            @PathVariable Long id,
            @RequestBody ConferenciaDTO.ActualizarRequest request) {
        return ResponseEntity.ok(conferenciaService.actualizar(id, request));
    }

    // DELETE /api/conferencias/{id} — solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        conferenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Helper: obtener ID del estudiante autenticado (null si no está autenticado) ──
    private Long obtenerEstudianteId(Principal principal) {
        if (principal == null) return null;
        return usuarioRepository.findByCorreo(principal.getName())
                .filter(u -> u.getRol() == Usuario.Rol.ESTUDIANTE)
                .map(Usuario::getId)
                .orElse(null);
    }
}
