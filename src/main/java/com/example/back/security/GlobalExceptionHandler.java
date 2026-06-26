package com.example.back.security;

// 📁 src/main/java/com/example/back/security/GlobalExceptionHandler.java

import com.example.back.dto.ErrorDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import java.util.stream.Collectors;

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

    // ── Errores de validación con @Valid ──────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        String mensajesPersonalizados = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));

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
                .mensaje(mensajesPersonalizados)
                .campos(campos)
                .build());
    }

    // ✅ NUEVO: Capturar errores de validación durante la persistencia (JPA/Hibernate)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolation(ConstraintViolationException ex) {
        // Extraer solo los mensajes personalizados de las validaciones
        String mensajesPersonalizados = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(Collectors.joining(", "));

        // También puedes obtener los nombres de los campos que fallaron
        Map<String, String> campos = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            // Extraer solo el nombre del campo (última parte después del último punto)
            String fieldName = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf(".") + 1)
                    : propertyPath;
            campos.put(fieldName, violation.getMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Error de validación")
                .mensaje(mensajesPersonalizados)
                .campos(campos.isEmpty() ? null : campos)
                .build());
    }

    // ── Errores de negocio (RuntimeException) ─────────────────────────────────
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