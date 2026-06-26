package com.example.back.service;

import com.example.back.model.PasswordResetToken;
import com.example.back.model.Usuario;
import com.example.back.repository.PasswordResetTokenRepository;
import com.example.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// src/main/java/com/tuapp/service/PasswordResetService.java
@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired private PasswordResetTokenRepository tokenRepo;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ── Paso 1: generar token y enviar correo ──────────────────────────────
    public void solicitarRecuperacion(String correo) {
        // Busca el usuario (si no existe, no revelamos nada)
        Optional<Usuario> opt = usuarioRepo.findByCorreo(correo);
        if (opt.isEmpty()) return;

        Usuario usuario = opt.get();

        // Borra tokens anteriores del mismo usuario
        tokenRepo.deleteByUsuario(usuario);

        // Genera token seguro
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpiraEn(LocalDateTime.now().plusHours(1));
        tokenRepo.save(resetToken);

        // Envía el correo
        enviarCorreo(correo, token);
    }

    // ── Paso 2: validar token y actualizar contraseña ──────────────────────
    public void resetearPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        if (resetToken.isUsado()) {
            throw new RuntimeException("Este enlace ya fue utilizado");
        }
        if (resetToken.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace ha expirado");
        }

        // Actualiza la contraseña
        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepo.save(usuario);

        // Marca el token como usado
        resetToken.setUsado(true);
        tokenRepo.save(resetToken);
    }

    // ── Helper: armar y enviar el correo ──────────────────────────────────
    private void enviarCorreo(String destino, String token) {
        //String link = frontendUrl + "/reset-password?token=" + token;
        String link = "http://10.36.9.83:5173" + "/reset-password?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destino);
        mensaje.setSubject("Recuperación de contraseña – UMB");
        mensaje.setText(
                "Hola,\n\n" +
                        "Haz clic en el siguiente enlace para restablecer tu contraseña:\n\n" +
                        link + "\n\n" +
                        "Este enlace expira en 1 hora.\n\n" +
                        "Si no solicitaste esto, ignora este correo."
        );
        mailSender.send(mensaje);
    }
}