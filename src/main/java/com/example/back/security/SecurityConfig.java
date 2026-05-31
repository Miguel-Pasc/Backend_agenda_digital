package com.example.back.security;

// 📁 src/main/java/com/example/back/security/SecurityConfig.java

import com.example.back.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Permitir todas las peticiones OPTIONS (preflight CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Rutas completamente públicas ──────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/recuperar-password",   // ← agregar
                                "/api/auth/reset-password"         // ← agregar
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/semanas/activa").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/semanas/*/conferencias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/conferencias/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pdf/agenda/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/pdf/agenda/**").permitAll()

                        // ── Estudiante autenticado ────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/inscripciones").hasRole("ESTUDIANTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/inscripciones/**").hasRole("ESTUDIANTE")
                        .requestMatchers(HttpMethod.GET,    "/api/inscripciones/agenda/**").hasAnyRole("ESTUDIANTE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/usuarios/me").hasRole("ESTUDIANTE")
                        .requestMatchers(HttpMethod.PUT,    "/api/auth/cambiar-password").hasAnyRole("ESTUDIANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/pdf/agenda-personal/**").hasAnyRole("ESTUDIANTE", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/semanas/*/conferencias/mis").hasRole("ESTUDIANTE")
                        .requestMatchers(HttpMethod.PUT,    "/api/usuarios/me/password").hasRole("ESTUDIANTE")

                        // ── Solo admin ────────────────────────────────────────────────
                        .requestMatchers("/api/semanas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/conferencias").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/conferencias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/conferencias/**").hasRole("ADMIN")
                        .requestMatchers("/api/conferencistas/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/inscripciones/conferencia/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // ── CORS configurado directamente en Spring Security ─────────────────────
    // Esto reemplaza el CorsConfig.java separado y evita conflictos
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://tu-app.vercel.app"   // ← cambiar al subir a Vercel
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}