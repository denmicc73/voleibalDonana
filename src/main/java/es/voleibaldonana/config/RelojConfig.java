package es.voleibaldonana.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj de la aplicación (inyectable para poder fijar la hora en los tests). */
@Configuration
public class RelojConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
