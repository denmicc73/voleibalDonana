package es.voleibaldonana.dto;

import es.voleibaldonana.model.NivelPatrocinio;

/**
 * Patrocinador listo para mostrar.
 *
 * @param logoUrl ruta del logo con su versión (nula si no tiene logo)
 */
public record PatrocinadorFila(
        Long id,
        String nombre,
        String web,
        NivelPatrocinio nivel,
        int orden,
        boolean visible,
        String logoUrl) {

    public boolean principal() {
        return nivel == NivelPatrocinio.PRINCIPAL;
    }
}
