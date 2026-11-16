package es.voleibaldonana.dto;

/** Datos de la persona conectada que se muestran en la cabecera del panel. */
public record UsuarioActual(String nombre, String email, boolean admin) {
}
