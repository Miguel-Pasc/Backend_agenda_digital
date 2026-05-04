package com.example.back.dto;

// 📁 src/main/java/com/example/back/dto/ErrorDTO.java
//
// Usado por GlobalExceptionHandler para devolver errores de forma consistente.

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private Map<String, String> campos;  // errores de validación campo por campo
}
