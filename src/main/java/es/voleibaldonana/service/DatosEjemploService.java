package es.voleibaldonana.service;

import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

/**
 * Datos de EJEMPLO para maquetar la portada (Fase 1).
 *
 * PROVISIONAL: se sustituye en las fases 3–6 por las entidades reales (categorías por
 * temporada, partidos y resultados) gestionadas desde el panel de administración.
 * Los rivales, fechas, resultados y horarios son inventados. Las categorías son las
 * oficiales de voleibol en Andalucía con años de nacimiento ORIENTATIVOS para la
 * temporada 2026/27 (el club los ajustará cada temporada).
 */
@Service
public class DatosEjemploService {

    static final String CLUB = "Almonte Doñana";

    /** Categoría de juego con edades y horario orientativos. */
    public record Categoria(String id, String nombre, String edad, String nacidos, String horario) {
    }

    /** Partido de la lista del calendario. Si {@code tanteo} es nulo, aún no se ha jugado. */
    public record Partido(String fecha, String local, String visitante, String tanteo, String hora, String estado) {

        public boolean jugado() {
            return tanteo != null;
        }
    }

    /** Lista de partidos de una categoría (una pestaña del calendario). */
    public record CalendarioCategoria(Categoria categoria, List<Partido> partidos) {
    }

    /** Último resultado destacado en la cabecera de la portada. */
    public record UltimoResultado(String categoria, String jornada, String local, String visitante,
            String tanteo, boolean victoria, String fecha) {
    }

    /** Próximo partido destacado en la cabecera de la portada. */
    public record ProximoPartido(String local, String visitante, String detalle) {
    }

    private static final Categoria SENIOR = new Categoria("senior", "Sénior", "Primer equipo",
            "Nacidos en 2006 y antes", "Mar. y jue. · 20:30 h");
    private static final Categoria JUNIOR = new Categoria("junior", "Júnior", "18–19 años",
            "Nacidos en 2007 y 2008", "Mar. y jue. · 20:30 h");
    private static final Categoria JUVENIL = new Categoria("juvenil", "Juvenil", "16–17 años",
            "Nacidos en 2009 y 2010", "Mar. y jue. · 19:00 h");
    private static final Categoria CADETE = new Categoria("cadete", "Cadete", "14–15 años",
            "Nacidos en 2011 y 2012", "Lun. y mié. · 18:00 h");
    private static final Categoria INFANTIL = new Categoria("infantil", "Infantil", "12–13 años",
            "Nacidos en 2013 y 2014", "Lun. y mié. · 17:00 h");
    private static final Categoria ALEVIN = new Categoria("alevin", "Alevín", "10–11 años",
            "Nacidos en 2015 y 2016", "Vie. · 17:00 h");

    public String temporada() {
        return "Temporada 2026 / 27";
    }

    public List<Categoria> categorias() {
        return List.of(SENIOR, JUNIOR, JUVENIL, CADETE, INFANTIL, ALEVIN);
    }

    public UltimoResultado ultimoResultado() {
        return new UltimoResultado("Sénior", "J08", CLUB, "CD Rival", "3 — 1", true,
                "Domingo 08 de noviembre · En casa");
    }

    public ProximoPartido proximoPartido() {
        return new ProximoPartido("CD Visitante", CLUB, "Dom. 15 nov. · 12:00 h · Campo rival");
    }

    public List<CalendarioCategoria> calendario() {
        return List.of(
                new CalendarioCategoria(SENIOR, List.of(
                        new Partido("08 nov", CLUB, "CD Rival", "3 – 1", null, "Final"),
                        new Partido("15 nov", "CD Visitante", CLUB, null, "12:00", "Fuera"),
                        new Partido("22 nov", CLUB, "CD Norte", null, "11:30", "En casa"))),
                new CalendarioCategoria(JUNIOR, List.of(
                        new Partido("07 nov", "CD Sur", CLUB, "1 – 3", null, "Final"),
                        new Partido("21 nov", CLUB, "CD Costa", null, "18:00", "En casa"))),
                new CalendarioCategoria(JUVENIL, List.of(
                        new Partido("08 nov", CLUB, "CD Norte", "2 – 3", null, "Final"),
                        new Partido("14 nov", "CD Marisma", CLUB, null, "17:00", "Fuera"),
                        new Partido("28 nov", CLUB, "CD Sur", null, "12:00", "En casa"))),
                new CalendarioCategoria(CADETE, List.of(
                        new Partido("07 nov", CLUB, "CD Costa", "3 – 0", null, "Final"),
                        new Partido("21 nov", "CD Rival", CLUB, null, "11:00", "Fuera"))),
                new CalendarioCategoria(INFANTIL, List.of(
                        new Partido("14 nov", CLUB, "CD Marisma", null, "10:30", "En casa"),
                        new Partido("28 nov", "CD Norte", CLUB, null, "10:00", "Fuera"))),
                new CalendarioCategoria(ALEVIN, List.of(
                        new Partido("21 nov", CLUB, "CD Sur", null, "10:00", "En casa"))));
    }

    /** Años de nacimiento para el formulario: de 6 a 70 años, del más reciente al más antiguo. */
    public List<Integer> aniosNacimiento() {
        int actual = Year.now().getValue();
        return IntStream.iterate(actual - 6, anio -> anio >= actual - 70, anio -> anio - 1)
                .boxed()
                .toList();
    }
}
