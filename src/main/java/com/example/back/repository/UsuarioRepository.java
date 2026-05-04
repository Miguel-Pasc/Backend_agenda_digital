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

    // Login — buscar por correo
    Optional<Usuario> findByCorreo(String correo);

    // Verificar si ya existe un correo registrado
    boolean existsByCorreo(String correo);

    // Verificar si ya existe una matrícula registrada
    boolean existsByMatricula(String matricula);

    // Verificar si ya existe un número de empleado registrado
    boolean existsByNumeroEmpleado(String numeroEmpleado);

    // Listar todos los estudiantes
    List<Usuario> findByRol(Rol rol);

    // Listar estudiantes por carrera
    List<Usuario> findByRolAndCarrera(Rol rol, Carrera carrera);

    // Buscar estudiante por matrícula
    Optional<Usuario> findByMatricula(String matricula);

    // Listar solo usuarios activos por rol
    List<Usuario> findByRolAndActivo(Rol rol, Boolean activo);
}