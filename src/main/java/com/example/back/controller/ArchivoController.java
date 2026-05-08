package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/ArchivoController.java
//
// Endpoint para subir imágenes desde el frontend.
// En local guarda en la carpeta uploads/.
// En producción se puede cambiar para usar Cloudinary.

import com.example.back.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoController {

    private final FileStorageService fileStorageService;

    // POST /api/archivos/subir?tipo=conferencistas
    // tipo puede ser: conferencistas, logos
    @PostMapping("/subir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> subir(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "tipo", defaultValue = "general") String tipo) {
        try {
            String url = fileStorageService.guardar(archivo, tipo);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}