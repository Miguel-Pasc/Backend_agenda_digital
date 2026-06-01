package com.example.back.security;

// 📁 src/main/java/com/example/back/security/GlobalExceptionHandler.java
//
// Centraliza todos los errores del sistema y los devuelve en formato consistente
// usando ErrorDTO para que el frontend siempre reciba la misma estructura.

import com.example.back.dto.ErrorDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Recurso no encontrado (EntityNotFoundException) ───────────────────────
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(404)
                .error("No encontrado")
                .mensaje(ex.getMessage())
                .build());
    }

    // ── Credenciales incorrectas ──────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDTO> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(401)
                .error("No autorizado")
                .mensaje("Identificador o contraseña incorrectos")
                .build());
    }

    // ── Usuario desactivado ───────────────────────────────────────────────────
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDTO> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(401)
                .error("Cuenta desactivada")
                .mensaje("Tu cuenta ha sido desactivada. Contacta al administrador.")
                .build());
    }

    // ── Sin permisos (403) ────────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(403)
                .error("Acceso denegado")
                .mensaje("No tienes permiso para realizar esta acción")
                .build());
    }

    // ── Errores de validación (@Valid) ────────────────────────────────────────
    // Devuelve un mapa campo → mensaje de error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            campos.put(field, message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Error de validación")
                .mensaje("Uno o más campos no son válidos")
                .campos(campos)
                .build());
    }

    // ── Errores de negocio (RuntimeException) ─────────────────────────────────
    // Cubre: cupo lleno, cruce de horario, correo duplicado, etc.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Error en la operación")
                .mensaje(ex.getMessage())
                .build());
    }

    // ── Cualquier otro error inesperado ───────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .error("Error interno del servidor")
                .mensaje("Ocurrió un error inesperado. Intenta de nuevo.")
                .build());
    }
}
