package com.example.back.repository;

// 📁 src/main/java/com/example/back/repository/ConferencistaRepository.java

import com.example.back.model.Conferencista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferencistaRepository extends JpaRepository<Conferencista, Long> {

    // Todos los conferencistas de una conferencia
    List<Conferencista> findByConferenciaId(Long conferenciaId);

    // Buscar conferencista por nombre (para el buscador global)
    List<Conferencista> findByNombreContainingIgnoreCase(String nombre);
}