package es.voleibaldonana.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.voleibaldonana.model.Patrocinador;

/** "NivelDesc": PRINCIPAL va antes que COLABORADOR (se guardan como texto). */
public interface PatrocinadorRepository extends JpaRepository<Patrocinador, Long> {

    List<Patrocinador> findAllByOrderByNivelDescOrdenAscNombreAsc();

    List<Patrocinador> findByVisibleTrueOrderByNivelDescOrdenAscNombreAsc();
}
