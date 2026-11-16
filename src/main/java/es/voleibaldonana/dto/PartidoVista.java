package es.voleibaldonana.dto;

import java.time.LocalDateTime;
import java.util.List;

import es.voleibaldonana.model.EstadoPartido;
import es.voleibaldonana.model.Genero;

/**
 * Partido listo para mostrar. Todo va ya en orden local → visitante.
 *
 * @param diaMes   "08 nov"
 * @param hora     "12:00"
 * @param tanteo   sets ganados, p. ej. "3 – 1" (nulo si no ha terminado)
 * @param sets     puntos de cada set, p. ej. ["25-20", "23-25"]
 * @param victoria si ganó el club (nulo si no ha terminado)
 */
public record PartidoVista(
        Long id,
        Long categoriaId,
        String categoria,
        Genero genero,
        String local,
        String visitante,
        boolean enCasa,
        LocalDateTime fechaHora,
        String diaMes,
        String hora,
        String lugar,
        String jornada,
        EstadoPartido estado,
        String tanteo,
        List<String> sets,
        Boolean victoria) {

    public boolean finalizado() {
        return estado == EstadoPartido.FINALIZADO;
    }

    /** "Cadete Fem." */
    public String equipo() {
        return categoria + " " + genero.getCorto();
    }

    /** "25-20 · 23-25 · 25-18" */
    public String setsTexto() {
        return String.join(" · ", sets);
    }

    /** Lo que se ve en la columna de estado del calendario. */
    public String estadoTexto() {
        return switch (estado) {
            case PROGRAMADO -> enCasa ? "En casa" : "Fuera";
            default -> estado.getDescripcion();
        };
    }
}
