package es.voleibaldonana.dto;

import java.util.List;

/**
 * Datos de la portada.
 *
 * @param temporada        p. ej. "Temporada 2026 / 27" (nulo si no hay temporada activa)
 * @param ultimoResultado  nulo si aún no hay resultados
 * @param proximoPartido   nulo si no hay partidos programados
 */
public record Portada(
        String temporada,
        UltimoResultado ultimoResultado,
        ProximoPartido proximoPartido,
        List<CategoriaFila> categorias,
        List<CalendarioCategoria> calendario,
        List<Integer> aniosNacimiento) {

    /** Último resultado destacado en la cabecera. */
    public record UltimoResultado(String etiqueta, String local, String visitante, String tanteo,
            String sets, boolean victoria, String fecha) {
    }

    /** Próximo partido destacado en la cabecera. */
    public record ProximoPartido(String equipo, String local, String visitante, String detalle) {
    }

    /** Una pestaña del calendario: los partidos de una categoría. */
    public record CalendarioCategoria(CategoriaFila categoria, List<PartidoVista> partidos) {
    }
}
