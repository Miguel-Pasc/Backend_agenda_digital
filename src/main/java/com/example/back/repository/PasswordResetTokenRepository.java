package com.example.back.repository;

import com.example.back.model.PasswordResetToken;
import com.example.back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// src/main/java/com/tuapp/repository/PasswordResetTokenRepository.java
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // Limpia tokens viejos del mismo usuario al generar uno nuevo
    void deleteByUsuario(Usuario usuario);
}
