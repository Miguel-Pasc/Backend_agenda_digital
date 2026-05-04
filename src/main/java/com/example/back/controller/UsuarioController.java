package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/UsuarioController.java

import com.example.back.dto.UsuarioDTO;
import com.example.back.model.Carrera;
import com.example.back.model.Usuario.Rol;
import com.example.back.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // POST /api/usuarios — solo ADMIN
    // Crea cualquier tipo de usuario (estudiante o admin)
    @PostMapping
    public ResponseEntity<UsuarioDTO.Response> crear(
            @Valid @RequestBody UsuarioDTO.CrearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crear(request));
    }

    // GET /api/usuarios — solo ADMIN
    // Listar todos, con filtro opcional por rol
    @GetMapping
    public ResponseEntity<List<UsuarioDTO.ResumenResponse>> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Carrera carrera) {
        if (carrera != null) {
            return ResponseEntity.ok(usuarioService.listarEstudiantesPorCarrera(carrera));
        }
        if (rol != null) {
            return ResponseEntity.ok(usuarioService.listarPorRol(rol));
        }
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // GET /api/usuarios/{id} — solo ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO.Response> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // PUT /api/usuarios/me — estudiante autenticado (actualiza su propio correo)
    @PutMapping("/me")
    public ResponseEntity<UsuarioDTO.Response> actualizarPropios(
            @RequestBody UsuarioDTO.ActualizarRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                usuarioService.actualizarPropios(principal.getName(), request));
    }

    // PUT /api/usuarios/{id} — solo ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO.Response> actualizarPorAdmin(
            @PathVariable Long id,
            @RequestBody UsuarioDTO.ActualizarAdminRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPorAdmin(id, request));
    }

    // DELETE /api/usuarios/{id} — solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/usuarios/{id}/desactivar — solo ADMIN (soft delete)
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
