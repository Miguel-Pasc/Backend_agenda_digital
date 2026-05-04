package com.example.back.repository;

// 📁 src/main/java/com/example/back/repository/SemanaAcademicaRepository.java

import com.example.back.model.SemanaAcademica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemanaAcademicaRepository extends JpaRepository<SemanaAcademica, Long> {

    // Obtener la semana activa actualmente
    Optional<SemanaAcademica> findByActivaTrue();

    // Verificar si ya existe una semana para ese número y año
    boolean existsByNumeroAndAnio(Integer numero, Integer anio);

    // Listar todas ordenadas por año y número descendente (más reciente primero)
    List<SemanaAcademica> findAllByOrderByAnioDescNumeroDesc();

    // Listar por año
    List<SemanaAcademica> findByAnioOrderByNumeroDesc(Integer anio);

    // Desactivar todas las semanas (se usa antes de activar una nueva)
    @Modifying
    @Query("UPDATE SemanaAcademica s SET s.activa = false")
    void desactivarTodas();
}