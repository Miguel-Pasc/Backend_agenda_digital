package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/ConferencistaController.java

import com.example.back.dto.ConferencistaDTO;
import com.example.back.model.Conferencista;
import com.example.back.repository.ConferencistaRepository;
import com.example.back.service.ConferenciaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conferencistas")
@RequiredArgsConstructor
public class ConferencistaController {

    private final ConferencistaRepository conferencistaRepository;
    private final ConferenciaService conferenciaService;

    // POST /api/conferencistas?conferenciaId={id} — solo ADMIN
    // Agrega un conferencista a una conferencia existente
    @PostMapping
    public ResponseEntity<ConferencistaDTO.Response> agregar(
            @RequestParam Long conferenciaId,
            @Valid @RequestBody ConferencistaDTO.Request request) {

        var conferencia = conferenciaService.buscarPorId(conferenciaId);

        Conferencista conferencista = Conferencista.builder()
                .nombre(request.getNombre())
                .perfilProfesional(request.getPerfilProfesional())
                .biografia(request.getBiografia())
                .fotografiaUrl(request.getFotografiaUrl())
                .logoUrl(request.getLogoUrl())
                .conferencia(conferencia)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(conferencistaRepository.save(conferencista)));
    }

    // GET /api/conferencistas?conferenciaId={id} — solo ADMIN
    // Lista todos los conferencistas de una conferencia
    @GetMapping
    public ResponseEntity<List<ConferencistaDTO.Response>> listarPorConferencia(
            @RequestParam Long conferenciaId) {
        return ResponseEntity.ok(
                conferencistaRepository.findByConferenciaId(conferenciaId)
                        .stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // GET /api/conferencistas/{id} — solo ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<ConferencistaDTO.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(buscarPorId(id)));
    }

    // PUT /api/conferencistas/{id} — solo ADMIN
    // Permite actualizar cualquier campo, incluyendo foto y logo que pueden
    // haberse dejado vacíos al crear el conferencista
    @PutMapping("/{id}")
    public ResponseEntity<ConferencistaDTO.Response> actualizar(
            @PathVariable Long id,
            @RequestBody ConferencistaDTO.ActualizarRequest request) {

        Conferencista conferencista = buscarPorId(id);

        if (request.getNombre() != null)            conferencista.setNombre(request.getNombre());
        if (request.getPerfilProfesional() != null) conferencista.setPerfilProfesional(request.getPerfilProfesional());
        if (request.getBiografia() != null)         conferencista.setBiografia(request.getBiografia());
        if (request.getFotografiaUrl() != null)     conferencista.setFotografiaUrl(request.getFotografiaUrl());
        if (request.getLogoUrl() != null)           conferencista.setLogoUrl(request.getLogoUrl());

        return ResponseEntity.ok(toResponse(conferencistaRepository.save(conferencista)));
    }

    // DELETE /api/conferencistas/{id} — solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!conferencistaRepository.existsById(id)) {
            throw new EntityNotFoundException("Conferencista no encontrado con id: " + id);
        }
        conferencistaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Conferencista buscarPorId(Long id) {
        return conferencistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Conferencista no encontrado con id: " + id));
    }

    private ConferencistaDTO.Response toResponse(Conferencista c) {
        return ConferencistaDTO.Response.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .perfilProfesional(c.getPerfilProfesional())
                .biografia(c.getBiografia())
                .fotografiaUrl(c.getFotografiaUrl())
                .logoUrl(c.getLogoUrl())
                .build();
    }
}
