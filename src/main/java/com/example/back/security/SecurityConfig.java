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
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Rutas completamente públicas ──────────────────────────────
                // Login
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                // Ver conferencias y semanas (invitado, estudiante y admin)
                .requestMatchers(HttpMethod.GET, "/api/semanas/activa").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/semanas/*/conferencias").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/conferencias/*").permitAll()

                // Descargar PDF de agenda general (invitado)
                .requestMatchers(HttpMethod.GET, "/api/pdf/agenda/**").permitAll()

                // ── Rutas de estudiante autenticado ───────────────────────────
                // Inscribirse y cancelar inscripción
                .requestMatchers(HttpMethod.POST,   "/api/inscripciones").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.DELETE, "/api/inscripciones/**").hasRole("ESTUDIANTE")

                // Ver su propia agenda
                .requestMatchers(HttpMethod.GET, "/api/inscripciones/agenda/**")
                    .hasAnyRole("ESTUDIANTE", "ADMIN")

                // Actualizar sus propios datos (correo)
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/me").hasRole("ESTUDIANTE")

                // Cambiar contraseña propia
                .requestMatchers(HttpMethod.PUT, "/api/auth/cambiar-password")
                    .hasAnyRole("ESTUDIANTE", "ADMIN")

                // Descargar PDF de agenda personal
                .requestMatchers(HttpMethod.GET, "/api/pdf/agenda-personal/**")
                    .hasAnyRole("ESTUDIANTE", "ADMIN")

                // ── Rutas exclusivas del administrador ────────────────────────
                // CRUD de semanas académicas
                .requestMatchers("/api/semanas/**").hasRole("ADMIN")

                // CRUD de conferencias (GET es público arriba, el resto solo admin)
                .requestMatchers(HttpMethod.POST,   "/api/conferencias").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/conferencias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/conferencias/**").hasRole("ADMIN")

                // CRUD de conferencistas
                .requestMatchers("/api/conferencistas/**").hasRole("ADMIN")

                // CRUD de usuarios
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // Ver inscripciones de una conferencia
                .requestMatchers(HttpMethod.GET, "/api/inscripciones/conferencia/**")
                    .hasRole("ADMIN")

                // ── Cualquier otra ruta requiere autenticación ────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(authenticationProvider());

        return http.build();
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
