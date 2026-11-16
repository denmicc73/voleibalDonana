package es.voleibaldonana.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj de la aplicación, siempre en hora peninsular española: las fechas de los partidos se
 * guardan en hora local y el servidor de producción suele estar en UTC.
 * Inyectable para poder fijar la hora en los tests.
 */
@Configuration
public class RelojConfig {

    public static final ZoneId ZONA_CLUB = ZoneId.of("Europe/Madrid");

    @Bean
    Clock clock() {
        return Clock.system(ZONA_CLUB);
    }
}
