package com.example.back.service;

// 📁 src/main/java/com/example/back/service/PdfAgendaService.java

import com.example.back.model.Conferencia;
import com.example.back.model.SemanaAcademica;
import com.example.back.repository.ConferenciaRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfAgendaService {

    private final ConferenciaRepository conferenciaRepository;
    private final SemanaAcademicaService semanaService;

    // Colores del programa (verde UMB)
    private static final DeviceRgb VERDE_HEADER  = new DeviceRgb(76, 153, 76);
    private static final DeviceRgb VERDE_CLARO   = new DeviceRgb(198, 239, 206);
    private static final DeviceRgb VERDE_MEDIO   = new DeviceRgb(169, 209, 142);
    private static final DeviceRgb BLANCO        = new DeviceRgb(255, 255, 255);

    private static final Locale LOCALE_ES = new Locale("es", "MX");

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
        doc.setMargins(36, 36, 36, 36);

        PdfFont fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontItalic  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        boolean primeraPagina = true;

        for (Map.Entry<Integer, List<Conferencia>> entry : porDia.entrySet()) {
            Integer dia        = entry.getKey();
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
            // Capitalizar primera letra
            fechaDia = fechaDia.substring(0, 1).toUpperCase() + fechaDia.substring(1);

            agregarTabla(doc, fechaDia, conferencias, fontBold, fontNormal, fontItalic);

            // ── PIE DE PÁGINA CON LOGOS DE CONFERENCISTAS ─────────────────────
            agregarPie(doc, conferencias, semana, fontBold, fontNormal, fontItalic);
        }

        doc.close();
        return baos.toByteArray();
    }

    // ── Encabezado ────────────────────────────────────────────────────────────
    private void agregarEncabezado(Document doc, SemanaAcademica semana,
                                   PdfFont fontBold, PdfFont fontNormal) throws Exception {
        // Fila de logos
        Table logoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

        logoTable.addCell(celdaImagen(semana.getLogoEstadoUrl(), "LOGO EDOMEX", fontNormal));
        logoTable.addCell(celdaImagen(semana.getLogoJornadaUrl(), "LOGO JORNADA", fontNormal));
        logoTable.addCell(celdaImagen(semana.getLogoUesUrl(), "LOGO UES", fontNormal));
        doc.add(logoTable);

        // Número de semana y año
        String ordinal = semana.getNumero() + "va";
        doc.add(new Paragraph(ordinal + " JORNADA ACADÉMICA Y CULTURAL " + semana.getAnio())
                .setFont(fontBold).setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(new Paragraph("UNIDAD DE ESTUDIOS SUPERIORES SAN JOSÉ DEL RINCÓN")
                .setFont(fontBold).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));
    }

    // ── Pie de página ─────────────────────────────────────────────────────────
    private void agregarPie(Document doc, List<Conferencia> conferencias,
                            SemanaAcademica semana,
                            PdfFont fontBold, PdfFont fontNormal, PdfFont fontItalic) {
        // Frase dinámica desde la BD, con fallback si no se configuró
        String frase = (semana.getFrasePie() != null && !semana.getFrasePie().isBlank())
                ? "\"" + semana.getFrasePie() + "\""
                : "\"CULTURA QUE INSPIRA, CONOCIMIENTO QUE TRANSFORMA\"";

        doc.add(new Paragraph(frase)
                .setFont(fontBold).setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8).setMarginBottom(8));

        // Logos de conferencistas que tienen logoUrl
        List<String> logos = conferencias.stream()
                .filter(c -> c.getLogoUrl() != null && !c.getLogoUrl().isBlank())
                .map(Conferencia::getLogoUrl)
                .distinct()
                .collect(Collectors.toList());

        if (!logos.isEmpty()) {
            int cols = logos.size();
            Table logoTable = new Table(UnitValue.createPercentArray(new float[cols]))
                    .setWidth(UnitValue.createPercentValue(100));

            logos.forEach(url -> {
                try {
                    logoTable.addCell(celdaImagen(url, "", fontItalic));
                } catch (Exception e) {
                    logoTable.addCell(new Cell().setBorder(Border.NO_BORDER));
                }
            });
            doc.add(logoTable);
        }
    }

    // ── Tabla de conferencias ─────────────────────────────────────────────────
    private void agregarTabla(Document doc, String fechaDia,
                              List<Conferencia> conferencias,
                              PdfFont fontBold, PdfFont fontNormal, PdfFont fontItalic) {

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{25, 75}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Fila header con la fecha del día
        Cell headerHorario = new Cell()
                .add(new Paragraph("Horario").setFont(fontBold).setFontSize(11).setFontColor(BLANCO))
                .setBackgroundColor(VERDE_HEADER)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);

        Cell headerFecha = new Cell()
                .add(new Paragraph(fechaDia).setFont(fontBold).setFontSize(11).setFontColor(BLANCO))
                .setBackgroundColor(VERDE_HEADER)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);

        tabla.addHeaderCell(headerHorario);
        tabla.addHeaderCell(headerFecha);

        // Filas de conferencias alternando colores
        for (int i = 0; i < conferencias.size(); i++) {
            Conferencia c = conferencias.get(i);
            DeviceRgb fondo = (i % 2 == 0) ? VERDE_CLARO : VERDE_MEDIO;

            // Columna horario
            String horario = formatearHora(c.getHoraInicio()) + " a "
                    + formatearHora(c.getHoraFin()) + " hrs";

            Cell celdaHorario = new Cell()
                    .add(new Paragraph(horario).setFont(fontBold).setFontSize(10))
                    .setBackgroundColor(fondo)
                    .setPadding(8)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(BLANCO, 1));

            // Columna datos de la conferencia
            Cell celdaConf = new Cell().setBackgroundColor(fondo)
                    .setPadding(8)
                    .setBorder(new SolidBorder(BLANCO, 1));

            // Nombre de la conferencia
            celdaConf.add(new Paragraph(c.getNombre())
                    .setFont(fontBold).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            // Conferencistas
            if (c.getConferencistas() != null && !c.getConferencistas().isEmpty()) {
                c.getConferencistas().forEach(conf -> {
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
                });
            }

            // Escenario en negrita al final
            String escenario = formatearEscenario(c.getEscenario());
            celdaConf.add(new Paragraph(escenario)
                    .setFont(fontBold).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2));

            tabla.addCell(celdaHorario);
            tabla.addCell(celdaConf);
        }

        doc.add(tabla);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Cell celdaLogo(String texto, PdfFont font) {
        return new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1))
                .setPadding(6);
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

    private Cell celdaImagen(String url, String textoFallback, PdfFont font) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (url != null && !url.isBlank()) {
            try {
                // Si la URL es relativa (empieza con /uploads/), construir ruta local
                String rutaLocal = url.startsWith("/uploads/")
                        ? System.getProperty("user.dir") + url.replace("/", java.io.File.separator)
                        : url;

                com.itextpdf.io.image.ImageData imageData =
                        com.itextpdf.io.image.ImageDataFactory.create(rutaLocal);
                com.itextpdf.layout.element.Image img =
                        new com.itextpdf.layout.element.Image(imageData)
                                .setMaxHeight(50)
                                .setAutoScale(true);
                cell.add(img).setTextAlignment(TextAlignment.CENTER);
            } catch (Exception e) {
                // Si falla la carga, mostrar texto
                cell.add(new Paragraph(textoFallback).setFont(font).setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER));
            }
        } else if (!textoFallback.isBlank()) {
            cell.add(new Paragraph(textoFallback).setFont(font).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        return cell;
    }
}