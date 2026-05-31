package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/PdfController.java

import com.example.back.service.PdfAgendaService;
import com.example.back.service.SemanaAcademicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfAgendaService pdfAgendaService;
    private final SemanaAcademicaService semanaService;

    // GET /api/pdf/agenda/{semanaId} — público
    @GetMapping("/agenda/{semanaId}")
    public ResponseEntity<byte[]> descargarAgenda(@PathVariable Long semanaId) {
        try {
            byte[] pdf = pdfAgendaService.generarAgenda(semanaId);

            String nombreArchivo = "agenda-semana-" + semanaId + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}