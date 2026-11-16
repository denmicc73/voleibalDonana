package es.voleibaldonana.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.voleibaldonana.model.SolicitudPatrocinio;

public interface SolicitudPatrocinioRepository extends JpaRepository<SolicitudPatrocinio, Long> {

    List<SolicitudPatrocinio> findAllByOrderByAtendidaAscRecibidaDesc();

    long countByAtendidaFalse();
}
