package es.voleibaldonana.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la consola H2, activa SOLO con el perfil "dev" y si la consola está habilitada.
 *
 * Es una cadena independiente que afecta únicamente a /h2-console/** (solo por ruta, sin
 * referenciar clases de h2console para compilar también sin el perfil Maven "dev"):
 * permite el uso de frames del mismo origen y desactiva CSRF solo para esa ruta.
 * El resto de la aplicación mantiene CSRF activo (ver SecurityConfig).
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class DevH2ConsoleSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/h2-console/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
