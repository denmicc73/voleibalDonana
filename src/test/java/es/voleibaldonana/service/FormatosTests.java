package es.voleibaldonana.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Textos de fechas y de años de nacimiento. */
class FormatosTests {

    private static final LocalDateTime DOMINGO = LocalDateTime.of(2026, 11, 8, 12, 0);

    @Test
    void fechasEnEspanol() {
        assertThat(FormatoFechas.larga(DOMINGO)).isEqualTo("Domingo 08 de noviembre");
        assertThat(FormatoFechas.corta(DOMINGO)).isEqualTo("Dom. 8 nov.");
        assertThat(FormatoFechas.diaMes(DOMINGO)).isEqualTo("08 nov");
        assertThat(FormatoFechas.hora(LocalDateTime.of(2026, 11, 14, 9, 5))).isEqualTo("09:05");
        assertThat(FormatoFechas.larga(LocalDateTime.of(2026, 11, 11, 18, 0))).isEqualTo("Miércoles 11 de noviembre");
    }

    @Test
    void textoDeNacidos() {
        assertThat(Vistas.nacidos(null, 2006)).isEqualTo("Nacidos en 2006 y antes");
        assertThat(Vistas.nacidos(2011, 2012)).isEqualTo("Nacidos en 2011 y 2012");
        assertThat(Vistas.nacidos(2015, 2015)).isEqualTo("Nacidos en 2015");
        assertThat(Vistas.nacidos(2010, 2013)).isEqualTo("Nacidos entre 2010 y 2013");
        assertThat(Vistas.nacidos(2018, null)).isEqualTo("Nacidos en 2018 y después");
        assertThat(Vistas.nacidos(null, null)).isNull();
    }
}
