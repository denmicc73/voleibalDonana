package es.voleibaldonana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad PROVISIONAL (Fase 0).
 *
 * Todavía no existe login (llegará en la Fase 2), así que de momento:
 * - todo es público excepto /admin/**, que queda bloqueado para todos;
 * - CSRF permanece activado (no se desactiva en ningún caso).
 *
 * Esta clase se sustituirá por la configuración real con usuarios y roles.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").denyAll()
                .anyRequest().permitAll());
        return http.build();
    }
}
