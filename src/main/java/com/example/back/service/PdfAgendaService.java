package com.example.back.service;

// 📁 src/main/java/com/example/back/service/PdfAgendaService.java

import com.example.back.model.Conferencia;
import com.example.back.model.Conferencista;
import com.example.back.model.SemanaAcademica;
import com.example.back.repository.ConferenciaRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfAgendaService {

    private final ConferenciaRepository conferenciaRepository;
    private final SemanaAcademicaService semanaService;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Value("${app.uploads.url:http://localhost:8081/uploads}")
    private String uploadsUrl;

    // Colores del programa (verde UMB)
    private static final DeviceRgb VERDE_HEADER = new DeviceRgb(76, 153, 76);
    private static final DeviceRgb VERDE_CLARO  = new DeviceRgb(198, 239, 206);
    private static final DeviceRgb VERDE_MEDIO  = new DeviceRgb(169, 209, 142);
    private static final DeviceRgb BLANCO       = new DeviceRgb(255, 255, 255);

    private static final Locale LOCALE_ES = new Locale("es", "MX");

    @Transactional(readOnly = true)
    public byte[] generarAgenda(Long semanaId) throws Exception {
        SemanaAcademica semana = semanaService.buscarPorId(semanaId);

        List<Conferencia> todasConferencias = conferenciaRepository
                .findBySemanaConConferencistasParaPdf(semanaId);

        // Agrupar por día
        Map<Integer, List<Conferencia>> porDia = todasConferencias.stream()
                .collect(Collectors.groupingBy(Conferencia::getDia,
                        TreeMap::new, Collectors.toList()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf, PageSize.LETTER);
        doc.setMargins(26, 36, 26, 36);

        PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        boolean primeraPagina = true;

        for (Map.Entry<Integer, List<Conferencia>> entry : porDia.entrySet()) {
            Integer dia = entry.getKey();
            List<Conferencia> conferencias = entry.getValue();

            if (!primeraPagina) {
                doc.add(new AreaBreak());
            }
            primeraPagina = false;

            // ── ENCABEZADO ────────────────────────────────────────────────────
            agregarEncabezado(doc, semana, fontBold, fontNormal);

            // ── TABLA DE CONFERENCIAS DEL DÍA ─────────────────────────────────
            String fechaDia = semana.getFechaDelDia(dia)
                    .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", LOCALE_ES));
            fechaDia = fechaDia.substring(0, 1).toUpperCase() + fechaDia.substring(1);

            agregarTabla(doc, fechaDia, conferencias, fontBold, fontNormal, fontItalic);

            // ── PIE DE PÁGINA CON FRASE + LOGOS DE CONFERENCISTAS ─────────────
            agregarPie(doc, conferencias, semana, fontBold, fontNormal, fontItalic);
        }

        doc.close();
        return baos.toByteArray();
    }


    /**
     * Convierte un número a su forma ordinal en español femenino (para "jornada")
     * Ejemplos: 1 -> "1ra", 2 -> "2da", 3 -> "3ra", 4 -> "4ta", 5 -> "5ta",
     *           9 -> "9na", 10 -> "10ma", 11 -> "11ra", 12 -> "12da", 13 -> "13ra"
     */
    private String obtenerOrdinal(int numero) {
        if (numero <= 0) return numero + "va";

        int ultimoDigito = numero % 10;
        int ultimosDos = numero % 100;

        // Casos especiales para números del 11 al 19
        if (ultimosDos >= 11 && ultimosDos <= 19) {
            return numero + "va";  // 11va, 12va, 13va, 14va, 15va, 16va, 17va, 18va, 19va
        }

        // Casos según el último dígito
        switch (ultimoDigito) {
            case 1:
                return numero + "ra";   // 1ra, 21ra, 31ra...
            case 2:
                return numero + "da";   // 2da, 22da, 32da...
            case 3:
                return numero + "ra";   // 3ra, 23ra, 33ra...
            case 4:
                return numero + "ta";   // 4ta, 24ta, 34ta...
            case 5:
                return numero + "ta";   // 5ta, 25ta, 35ta...
            case 6:
                return numero + "ta";   // 6ta, 26ta, 36ta...
            case 7:
                return numero + "ma";   // 7ma, 27ma, 37ma...
            case 8:
                return numero + "va";   // 8va, 28va, 38va...
            case 9:
                return numero + "na";   // 9na, 29na, 39na...
            case 0:
                return numero + "ma";   // 10ma, 20ma, 30ma...
            default:
                return numero + "va";   // fallback
        }
    }

    // ── Encabezado con logos ──────────────────────────────────────────────────
    private void agregarEncabezado(Document doc, SemanaAcademica semana,
                                   PdfFont fontBold, PdfFont fontNormal) throws Exception {
        Table logoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

        // ✅ Elimina setTextAlignment de la celda, ahora se maneja dentro de celdaImagen
        logoTable.addCell(celdaImagen(semana.getLogoEstadoUrl(), "LOGO EDOMEX", fontNormal, 40));
        logoTable.addCell(celdaImagen(semana.getLogoJornadaUrl(), "LOGO JORNADA", fontNormal, 40));
        logoTable.addCell(celdaImagen(semana.getLogoUesUrl(), "LOGO UES", fontNormal, 40));

        doc.add(logoTable);

        // Número de semana y año
        String ordinal = obtenerOrdinal(semana.getNumero());
        doc.add(new Paragraph(ordinal + " JORNADA ACADÉMICA Y CULTURAL " + semana.getAnio())
                .setFont(fontBold).setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(new Paragraph("UNIDAD DE ESTUDIOS SUPERIORES SAN JOSÉ DEL RINCÓN")
                .setFont(fontBold).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));
    }

    // ── Pie de página: frase centrada + logos en la parte inferior de la hoja ──
    // ── Pie de página: frase + logos en la parte inferior de la hoja ──
    // ── Pie de página: frase + logos en la parte inferior de la hoja ──
    private void agregarPie(Document doc, List<Conferencia> conferencias,
                            SemanaAcademica semana, PdfFont fontBold,
                            PdfFont fontNormal, PdfFont fontItalic) {

        // Logos de institución
        List<String> logos = conferencias.stream()
                .map(Conferencia::getLogoUrl)
                .filter(url -> url != null && !url.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // Frase dinámica
        String frase = (semana.getFrasePie() != null && !semana.getFrasePie().isBlank())
                ? semana.getFrasePie()
                : "CULTURA QUE INSPIRA, CONOCIMIENTO QUE TRANSFORMA";

        // Si no hay nada que mostrar, salir
        if (logos.isEmpty() && (frase == null || frase.isBlank())) return;

        // Parámetros fijos
        final float LOGO_H    = 36f;
        final float LOGO_GAP  = 20f;
        final float FRASE_Y_OFFSET = 12f;
        final float BOTTOM_MARGIN = 28f;
        final float PAGE_W    = PageSize.LETTER.getWidth();

        float logosBaseY = BOTTOM_MARGIN;

        // Pre-cargar imágenes
        List<ImageData> imgDatas = new ArrayList<>();
        List<Float>     widths   = new ArrayList<>();

        for (String url : logos) {
            ImageData id = cargarImagen(url);
            if (id != null) {
                float ratio = (float) id.getWidth() / (float) id.getHeight();
                float w = Math.min(LOGO_H * ratio, LOGO_H * 3f);
                imgDatas.add(id);
                widths.add(w);
            }
        }

        PdfPage page = doc.getPdfDocument().getLastPage();
        PdfCanvas canvas = new PdfCanvas(page);

        float fraseBaseY = logosBaseY;
        if (!imgDatas.isEmpty()) {
            fraseBaseY = logosBaseY + LOGO_H + FRASE_Y_OFFSET;
        }

        // Dibujar frase centrada
        if (frase != null && !frase.isBlank()) {
            String fraseConComillas = "\"" + frase + "\"";
            float fontSize = 9f;

            // Calcular ancho del texto
            float textWidth = fontBold.getWidth(fraseConComillas, fontSize);

            // Posición X centrada
            float fraseX = (PAGE_W - textWidth) / 2;

            // Dibujar el texto
            canvas.beginText();
            canvas.setFontAndSize(fontBold, fontSize);  // ✅ Directamente fontBold, sin .getPdfFont()
            canvas.setTextMatrix(fraseX, fraseBaseY);
            canvas.showText(fraseConComillas);
            canvas.endText();
        }

        // Dibujar logos
        if (!imgDatas.isEmpty()) {
            float totalW = widths.stream().reduce(0f, Float::sum)
                    + LOGO_GAP * (widths.size() - 1);
            float startX = (PAGE_W - totalW) / 2f;

            for (int i = 0; i < imgDatas.size(); i++) {
                float w = widths.get(i);
                canvas.addImageFittedIntoRectangle(
                        imgDatas.get(i),
                        new Rectangle(startX, logosBaseY, w, LOGO_H),
                        false
                );
                startX += w + LOGO_GAP;
            }
        }

        canvas.release();
    }

    // ── Tabla de conferencias con foto del conferencista ──────────────────────
    private void agregarTabla(Document doc, String fechaDia,
                              List<Conferencia> conferencias,
                              PdfFont fontBold, PdfFont fontNormal, PdfFont fontItalic) {

        // 3 columnas: Horario | Foto conferencista | Datos de la conferencia
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{20, 12, 68}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Fila encabezado — ocupa las 3 columnas
        Cell headerHorario = new Cell()
                .add(new Paragraph("Horario").setFont(fontBold).setFontSize(11).setFontColor(BLANCO))
                .setBackgroundColor(VERDE_HEADER)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);

        // Header "Foto"
        Cell headerFotoVacia = new Cell()
                .add(new Paragraph("Foto").setFont(fontBold).setFontSize(11).setFontColor(BLANCO))
                .setBackgroundColor(VERDE_HEADER)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER);

        Cell headerFecha = new Cell()
                .add(new Paragraph(fechaDia).setFont(fontBold).setFontSize(11).setFontColor(BLANCO))
                .setBackgroundColor(VERDE_HEADER)
                .setPadding(8)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        tabla.addHeaderCell(headerHorario);
        tabla.addHeaderCell(headerFotoVacia);
        tabla.addHeaderCell(headerFecha);

        // Filas de conferencias alternando colores
        for (int i = 0; i < conferencias.size(); i++) {
            Conferencia c = conferencias.get(i);
            DeviceRgb fondo = (i % 2 == 0) ? VERDE_CLARO : VERDE_MEDIO;

            String horario = formatearHora(c.getHoraInicio()) + " a "
                    + formatearHora(c.getHoraFin()) + " hrs";
            String escenario = formatearEscenario(c.getEscenario());

            List<Conferencista> confs = (c.getConferencistas() != null && !c.getConferencistas().isEmpty())
                    ? c.getConferencistas()
                    : List.of(); // lista vacía para garantizar al menos 1 fila

            int totalConfs = Math.max(confs.size(), 1);

            for (int j = 0; j < totalConfs; j++) {
                // ── Columna 1: Horario — solo en la primera fila del grupo (rowspan lógico) ──
                if (j == 0) {
                    // Primera fila del grupo: muestra horario con rowspan
                    Cell celdaHorario = new Cell(totalConfs, 1)
                            .add(new Paragraph(horario).setFont(fontBold).setFontSize(10))
                            .setBackgroundColor(fondo)
                            .setPadding(8)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setBorder(new SolidBorder(BLANCO, 1));
                    tabla.addCell(celdaHorario);
                }

                // ── Columna 2: Foto del conferencista j ──────────────────────────────
                Cell celdaFoto = new Cell()
                        .setBackgroundColor(fondo)
                        .setPadding(4)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(new SolidBorder(BLANCO, 1));

                if (j < confs.size()) {
                    Conferencista conf = confs.get(j);
                    if (conf.getFotografiaUrl() != null && !conf.getFotografiaUrl().isBlank()) {
                        try {
                            ImageData imgData = cargarImagen(conf.getFotografiaUrl());
                            if (imgData != null) {
                                Image foto = new Image(imgData)
                                        .setMaxHeight(55)
                                        .setMaxWidth(45)
                                        .setAutoScale(true);
                                celdaFoto.add(foto);
                            }
                        } catch (Exception ignored) { }
                    } else {
                        celdaFoto.add(new Paragraph("Sin foto"));
                    }
                }
                tabla.addCell(celdaFoto);

                // ── Columna 3: Datos ─────────────────────────────────────────────────
                Cell celdaConf = new Cell()
                        .setBackgroundColor(fondo)
                        .setPadding(8)
                        .setBorder(new SolidBorder(BLANCO, 1));

                if (j == 0) {
                    // Primera fila del grupo: nombre de la conferencia
                    celdaConf.add(new Paragraph(c.getNombre())
                            .setFont(fontBold).setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(2));
                }

                if (j < confs.size()) {
                    Conferencista conf = confs.get(j);
                    celdaConf.add(new Paragraph(conf.getNombre())
                            .setFont(fontNormal).setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(1));
                    if (conf.getPerfilProfesional() != null && !conf.getPerfilProfesional().isBlank()) {
                        celdaConf.add(new Paragraph(conf.getPerfilProfesional())
                                .setFont(fontItalic).setFontSize(8)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(1));
                    }
                }

                // Escenario solo en la última fila del grupo
                if (j == totalConfs - 1) {
                    celdaConf.add(new Paragraph(escenario)
                            .setFont(fontBold).setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(2));
                }

                tabla.addCell(celdaConf);
            }
        }

        doc.add(tabla);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Carga una imagen desde una URL HTTP o desde el filesystem local.
     * Soporta:
     *   - URL completa: http://localhost:8081/uploads/logos/abc.png
     *   - Ruta relativa: /uploads/logos/abc.png
     *   - URL externa: https://ejemplo.com/logo.png
     */
    private ImageData cargarImagen(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            // ── Caso 1: URL completa del propio servidor (http://localhost:8081/uploads/...) ──
            // NUNCA hacer la conexión HTTP — siempre resolver directo en disco.
            if (url.startsWith("http://") || url.startsWith("https://")) {
                if (url.contains("localhost") || url.contains("127.0.0.1")) {
                    // Extraer la ruta después de /uploads/
                    int idx = url.indexOf("/uploads/");
                    if (idx >= 0) {
                        String rutaRelativa = url.substring(idx + "/uploads/".length());
                        java.io.File archivo = Paths.get(uploadsDir, rutaRelativa).toFile();
                        if (archivo.exists()) {
                            return ImageDataFactory.create(archivo.getAbsolutePath());
                        }
                    }
                    // Si no encontró el archivo en disco, no intentar HTTP (deadlock)
                    return null;
                }
                // URL externa real (otro dominio) — sí descargar
                try (InputStream is = new URL(url).openStream()) {
                    return ImageDataFactory.create(is.readAllBytes());
                }
            }

            // ── Caso 2: Ruta relativa /uploads/subcarpeta/archivo.png ──
            if (url.startsWith("/uploads/")) {
                String rutaRelativa = url.substring("/uploads/".length());
                java.io.File archivo = Paths.get(uploadsDir, rutaRelativa).toFile();
                if (archivo.exists()) {
                    return ImageDataFactory.create(archivo.getAbsolutePath());
                }
                return null;
            }

            // ── Caso 3: Ruta absoluta de archivo ──
            java.io.File archivo = new java.io.File(url);
            if (archivo.exists()) {
                return ImageDataFactory.create(archivo.getAbsolutePath());
            }

        } catch (Exception e) {
            // Imagen no disponible — celda quedará vacía
        }
        return null;
    }

    private Cell celdaImagen(String url, String textoFallback, PdfFont font, float maxHeight) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0);  // Elimina padding para mejor centrado

        ImageData imgData = cargarImagen(url);
        if (imgData != null) {
            // Calcular proporción original
            float ratio = (float) imgData.getWidth() / (float) imgData.getHeight();
            float ancho = maxHeight * ratio;

            // Crear imagen con tamaño fijo (sin autoscale)
            Image img = new Image(imgData)
                    .setHeight(maxHeight)           // Altura fija
                    .setWidth(ancho)                // Ancho proporcional
                    .setAutoScale(false);           // No autoescalar

            // ✅ CLAVE: Centrar la imagen dentro de la celda
            img.setHorizontalAlignment(HorizontalAlignment.CENTER);

            cell.add(img);
        } else if (textoFallback != null && !textoFallback.isBlank()) {
            Paragraph fallback = new Paragraph(textoFallback)
                    .setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER);
            cell.add(fallback);
        }

        return cell;
    }

    private String formatearHora(java.time.LocalTime hora) {
        if (hora == null) return "";
        return String.format("%02d:%02d", hora.getHour(), hora.getMinute());
    }

    private String formatearEscenario(Conferencia.Escenario escenario) {
        if (escenario == null) return "";
        return switch (escenario) {
            case AULA_MAGNA       -> "Aula Magna";
            case SALA_DE_COMPUTO  -> "Sala de Cómputo";
            case ZONA_DE_CULTIVOS -> "Zona de Cultivos";
        };
    }
}