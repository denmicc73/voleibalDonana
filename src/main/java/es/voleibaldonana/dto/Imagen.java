package es.voleibaldonana.dto;

/** Imagen lista para servir: bytes y tipo MIME. */
public record Imagen(byte[] datos, String tipo) {
}
