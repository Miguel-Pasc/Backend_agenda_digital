package com.example.back.service;

// 📁 src/main/java/com/example/back/service/UsuarioService.java

import com.example.back.dto.UsuarioDTO;
import com.example.back.model.Carrera;
import com.example.back.model.Usuario;
import com.example.back.model.Usuario.Rol;
import com.example.back.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioDTO.Response crear(UsuarioDTO.CrearRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado: " + request.getCorreo());
        }
        if (request.getMatricula() != null &&
                usuarioRepository.existsByMatricula(request.getMatricula())) {
            throw new RuntimeException("La matrícula ya está registrada: " + request.getMatricula());
        }
        if (request.getNumeroEmpleado() != null &&
                usuarioRepository.existsByNumeroEmpleado(request.getNumeroEmpleado())) {
            throw new RuntimeException("El número de empleado ya está registrado: " + request.getNumeroEmpleado());
        }

        if (request.getRol() == Rol.ESTUDIANTE) {
            if (request.getMatricula() == null || request.getMatricula().isBlank())
                throw new RuntimeException("La matrícula es requerida para estudiantes");
            if (request.getCarrera() == null)
                throw new RuntimeException("La carrera es requerida para estudiantes");
        }
        if (request.getRol() == Rol.ADMIN) {
            if (request.getNumeroEmpleado() == null || request.getNumeroEmpleado().isBlank())
                throw new RuntimeException("El número de empleado es requerido para administradores");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .matricula(request.getMatricula())
                .carrera(request.getCarrera())
                .numeroEmpleado(request.getNumeroEmpleado())
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.ResumenResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream().map(this::toResumen).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.ResumenResponse> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol)
                .stream().map(this::toResumen).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.ResumenResponse> listarEstudiantesPorCarrera(Carrera carrera) {
        return usuarioRepository.findByRolAndCarrera(Rol.ESTUDIANTE, carrera)
                .stream().map(this::toResumen).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioDTO.Response obtenerPorId(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public UsuarioDTO.Response actualizarPropios(String correoActual, UsuarioDTO.ActualizarRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoActual)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (request.getCorreo() != null && !request.getCorreo().equals(correoActual)) {
            if (usuarioRepository.existsByCorreo(request.getCorreo()))
                throw new RuntimeException("El correo ya está en uso: " + request.getCorreo());
            usuario.setCorreo(request.getCorreo());
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarPassword(String correoActual, UsuarioDTO.CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoActual)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Verificar que nueva y confirmación coincidan
        if (!request.getPasswordNueva().equals(request.getPasswordConfirmar())) {
            throw new RuntimeException("La nueva contraseña y la confirmación no coinciden");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioDTO.Response actualizarPorAdmin(Long id, UsuarioDTO.ActualizarAdminRequest request) {
        Usuario usuario = buscarPorId(id);

        if (request.getNombre() != null)        usuario.setNombre(request.getNombre());
        if (request.getActivo() != null)        usuario.setActivo(request.getActivo());
        if (request.getCarrera() != null)       usuario.setCarrera(request.getCarrera());
        if (request.getMatricula() != null) {
            if (!request.getMatricula().equals(usuario.getMatricula()) &&
                    usuarioRepository.existsByMatricula(request.getMatricula()))
                throw new RuntimeException("La matrícula ya está en uso");
            usuario.setMatricula(request.getMatricula());
        }
        if (request.getNumeroEmpleado() != null) {
            if (!request.getNumeroEmpleado().equals(usuario.getNumeroEmpleado()) &&
                    usuarioRepository.existsByNumeroEmpleado(request.getNumeroEmpleado()))
                throw new RuntimeException("El número de empleado ya está en uso");
            usuario.setNumeroEmpleado(request.getNumeroEmpleado());
        }
        if (request.getCorreo() != null) {
            if (!request.getCorreo().equals(usuario.getCorreo()) &&
                    usuarioRepository.existsByCorreo(request.getCorreo()))
                throw new RuntimeException("El correo ya está en uso");
            usuario.setCorreo(request.getCorreo());
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id))
            throw new EntityNotFoundException("Usuario no encontrado con id: " + id);
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
    }

    public UsuarioDTO.Response toResponse(Usuario u) {
        return UsuarioDTO.Response.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .matricula(u.getMatricula())
                .numeroEmpleado(u.getNumeroEmpleado())
                .carrera(u.getCarrera())
                .activo(u.getActivo())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }

    private UsuarioDTO.ResumenResponse toResumen(Usuario u) {
        return UsuarioDTO.ResumenResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .matricula(u.getMatricula())
                .numeroEmpleado(u.getNumeroEmpleado())   // ← ahora incluido
                .carrera(u.getCarrera())
                .activo(u.getActivo())
                .build();
    }
}