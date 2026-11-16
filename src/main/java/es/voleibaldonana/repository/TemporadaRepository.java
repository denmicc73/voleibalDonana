package es.voleibaldonana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.voleibaldonana.model.Temporada;

public interface TemporadaRepository extends JpaRepository<Temporada, Long> {

    Optional<Temporada> findFirstByActivaTrue();

    List<Temporada> findAllByOrderByNombreDesc();

    boolean existsByNombre(String nombre);
}
