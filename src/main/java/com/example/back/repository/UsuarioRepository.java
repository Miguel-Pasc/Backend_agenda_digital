package com.example.back.repository;

// 📁 src/main/java/com/example/back/repository/UsuarioRepository.java

import com.example.back.model.Carrera;
import com.example.back.model.Usuario;
import com.example.back.model.Usuario.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Login y autenticación
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByMatricula(String matricula);
    Optional<Usuario> findByNumeroEmpleado(String numeroEmpleado); // ← nuevo

    // Verificaciones de unicidad
    boolean existsByCorreo(String correo);
    boolean existsByMatricula(String matricula);
    boolean existsByNumeroEmpleado(String numeroEmpleado);

    // Listados
    List<Usuario> findByRol(Rol rol);
    List<Usuario> findByRolAndCarrera(Rol rol, Carrera carrera);
    List<Usuario> findByRolAndActivo(Rol rol, Boolean activo);
}