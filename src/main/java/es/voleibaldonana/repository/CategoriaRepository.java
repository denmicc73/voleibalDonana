package es.voleibaldonana.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.voleibaldonana.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByTemporadaIdOrderByOrdenAscNombreAsc(Long temporadaId);

    List<Categoria> findByTemporadaIdAndVisibleTrueOrderByOrdenAscNombreAsc(Long temporadaId);

    boolean existsByTemporadaIdAndNombreIgnoreCase(Long temporadaId, String nombre);

    boolean existsByTemporadaIdAndNombreIgnoreCaseAndIdNot(Long temporadaId, String nombre, Long id);
}
