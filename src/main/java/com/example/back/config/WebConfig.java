package com.example.back.config;

// 📁 src/main/java/com/example/back/config/WebConfig.java
//
// Le dice a Spring Boot que sirva los archivos de la carpeta "uploads/"
// como recursos estáticos accesibles en http://localhost:8080/uploads/...

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convierte la ruta relativa a absoluta
        String rutaAbsoluta = Paths.get(uploadsDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(rutaAbsoluta);
    }
}