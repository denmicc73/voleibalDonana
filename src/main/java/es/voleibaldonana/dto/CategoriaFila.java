package es.voleibaldonana.dto;

/**
 * Categoría lista para mostrar (portada y panel).
 *
 * @param nacidos texto ya formado, p. ej. "Nacidos en 2011 y 2012" (nulo si no hay años)
 */
public record CategoriaFila(
        Long id,
        String nombre,
        String descripcionEdad,
        String nacidos,
        String horario,
        int orden,
        boolean visible) {

    /** "14–15 años · Nacidos en 2011 y 2012", con lo que haya. */
    public String edadYNacidos() {
        if (descripcionEdad == null) {
            return nacidos;
        }
        return nacidos == null ? descripcionEdad : descripcionEdad + " · " + nacidos;
    }
}
