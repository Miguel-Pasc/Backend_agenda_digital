package com.example.back.controller;

// 📁 src/main/java/com/example/back/controller/PdfController.java

import com.example.back.service.PdfAgendaService;
import com.example.back.service.SemanaAcademicaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final PdfAgendaService pdfAgendaService;
    private final SemanaAcademicaService semanaService;

    // GET /api/pdf/agenda/{semanaId} — público
    @GetMapping("/agenda/{semanaId}")
    public ResponseEntity<byte[]> descargarAgenda(@PathVariable Long semanaId) {
        try {
            log.info("Generando PDF para semana id={}", semanaId);
            byte[] pdf = pdfAgendaService.generarAgenda(semanaId);
            log.info("PDF generado exitosamente, tamaño={} bytes", pdf.length);

            String nombreArchivo = "agenda-semana-" + semanaId + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {
            log.error("Error al generar PDF para semana id={}: {}", semanaId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}