package es.voleibaldonana.service;

import java.time.LocalDateTime;

/**
 * Fechas en español escritas a mano (sin depender de los datos de idioma de la JVM,
 * que cambian entre versiones: "nov" / "nov.").
 */
public final class FormatoFechas {

    private static final String[] DIAS = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};
    private static final String[] DIAS_CORTOS = {"Lun.", "Mar.", "Mié.", "Jue.", "Vie.", "Sáb.", "Dom."};
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    private static final String[] MESES_CORTOS = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep",
            "oct", "nov", "dic"};

    private FormatoFechas() {
    }

    /** "Domingo 08 de noviembre". */
    public static String larga(LocalDateTime fecha) {
        String dia = DIAS[fecha.getDayOfWeek().getValue() - 1];
        return Character.toUpperCase(dia.charAt(0)) + dia.substring(1)
                + " %02d de %s".formatted(fecha.getDayOfMonth(), MESES[fecha.getMonthValue() - 1]);
    }

    /** "Dom. 15 nov." */
    public static String corta(LocalDateTime fecha) {
        return "%s %d %s.".formatted(DIAS_CORTOS[fecha.getDayOfWeek().getValue() - 1], fecha.getDayOfMonth(),
                MESES_CORTOS[fecha.getMonthValue() - 1]);
    }

    /** "08 nov" (lista del calendario). */
    public static String diaMes(LocalDateTime fecha) {
        return "%02d %s".formatted(fecha.getDayOfMonth(), MESES_CORTOS[fecha.getMonthValue() - 1]);
    }

    /** "12:00". */
    public static String hora(LocalDateTime fecha) {
        return "%02d:%02d".formatted(fecha.getHour(), fecha.getMinute());
    }
}
