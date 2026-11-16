package es.voleibaldonana.dto;

import java.time.LocalDateTime;

import es.voleibaldonana.model.Rol;

/** Fila del listado de usuarios (sin el hash de la contraseña). */
public record UsuarioResumen(
        Long id,
        String nombre,
        String email,
        Rol rol,
        boolean activo,
        boolean bloqueado,
        LocalDateTime ultimoAcceso) {
}
