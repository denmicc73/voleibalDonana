package es.voleibaldonana.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Protección del login frente a fuerza bruta.
 *
 * @param maxIntentos intentos fallidos seguidos antes de bloquear la cuenta
 * @param bloqueo     tiempo que la cuenta queda bloqueada
 */
@ConfigurationProperties("club.acceso")
public record AccesoProperties(
        @DefaultValue("5") int maxIntentos,
        @DefaultValue("15m") Duration bloqueo) {
}
