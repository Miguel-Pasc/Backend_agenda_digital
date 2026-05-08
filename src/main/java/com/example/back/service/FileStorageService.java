package com.example.back.service;

// 📁 src/main/java/com/example/back/service/FileStorageService.java

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Value("${app.uploads.url:http://localhost:8080/uploads}")
    private String uploadsUrl;

    /**
     * Guarda un archivo en la carpeta uploads y devuelve su URL pública.
     * @param file     Archivo recibido del frontend
     * @param subcarpeta  Ej: "conferencistas", "logos"
     * @return URL pública para acceder al archivo
     */
    public String guardar(MultipartFile file, String subcarpeta) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // Validar que sea una imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Solo se permiten archivos de imagen.");
        }

        // Crear carpeta si no existe
        Path carpeta = Paths.get(uploadsDir, subcarpeta);
        Files.createDirectories(carpeta);

        // Generar nombre único para evitar colisiones
        String extension  = obtenerExtension(file.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID().toString() + extension;
        Path rutaArchivo  = carpeta.resolve(nombreArchivo);

        // Guardar el archivo
        Files.copy(file.getInputStream(), rutaArchivo);

        // Devolver URL pública
        return uploadsUrl + "/" + subcarpeta + "/" + nombreArchivo;
    }

    /**
     * Elimina un archivo dado su URL pública.
     */
    public void eliminar(String url) {
        if (url == null || !url.startsWith(uploadsUrl)) return;
        try {
            String rutaRelativa = url.substring(uploadsUrl.length() + 1);
            Path ruta = Paths.get(uploadsDir, rutaRelativa);
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            // Si no se puede eliminar, no es crítico
        }
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) return ".jpg";
        return nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
    }
}