package es.voleibaldonana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.voleibaldonana.model.Rol;
import es.voleibaldonana.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRol(Rol rol);

    List<Usuario> findAllByOrderByNombreAsc();
}
