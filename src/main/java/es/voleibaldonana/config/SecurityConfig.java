package es.voleibaldonana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * Configuración de seguridad PROVISIONAL (Fase 0).
 *
 * Todavía no existe login (llegará en la Fase 2), así que de momento:
 * - todo es público excepto /admin/**, que queda bloqueado para todos;
 * - CSRF permanece activado (no se desactiva en ningún caso);
 * - se envían cabeceras de seguridad (CSP, Referrer-Policy, Permissions-Policy).
 *
 * Esta clase se sustituirá por la configuración real con usuarios y roles.
 *
 * Pendiente (Fase 9, según el hosting elegido): redirección a HTTPS y HSTS.
 */
@Configuration
public class SecurityConfig {

    /**
     * Política de contenido. Todo solo desde el propio origen.
     * Si en la Fase 1 se usan Google Fonts, ampliar aquí:
     * style-src con https://fonts.googleapis.com y font-src con https://fonts.gstatic.com.
     */
    static final String CONTENT_SECURITY_POLICY = "default-src 'self'; img-src 'self' data:; "
            + "style-src 'self'; script-src 'self'; font-src 'self'; frame-ancestors 'none'; "
            + "form-action 'self'; base-uri 'self'; object-src 'none'";

    static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";

    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").denyAll()
                        .anyRequest().permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY)));
        return http.build();
    }

    /**
     * Gestor de usuarios provisional y vacío: evita que Spring genere una contraseña
     * aleatoria en el log. Se sustituye en la Fase 2.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
