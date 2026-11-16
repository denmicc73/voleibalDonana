package es.voleibaldonana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import es.voleibaldonana.model.Partido;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    /** Todos los partidos de una temporada, con categoría y sets, por fecha. */
    @EntityGraph(attributePaths = {"categoria", "sets"})
    List<Partido> findByCategoriaTemporadaIdOrderByFechaHoraAsc(Long temporadaId);

    @EntityGraph(attributePaths = {"categoria", "sets"})
    List<Partido> findByCategoriaIdOrderByFechaHoraAsc(Long categoriaId);

    @EntityGraph(attributePaths = {"categoria", "sets"})
    Optional<Partido> findWithSetsById(Long id);

    boolean existsByCategoriaId(Long categoriaId);
}
