package es.voleibaldonana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Seguridad de la web.
 *
 * - La parte pública es libre; el panel (/admin/**) exige iniciar sesión.
 * - Roles: ADMIN entra en todo; EDITOR no entra en usuarios ni en inscripciones (datos de menores).
 * - Login propio en /admin/login (usuarios en BD, ver UsuarioDetailsService) y logout por POST.
 * - Bloqueo temporal tras varios intentos fallidos (ver IntentosAccesoService).
 * - CSRF siempre activo. Cabeceras: CSP, Referrer-Policy, Permissions-Policy y noindex en el panel.
 *
 * Pendiente (Fase 9, según el hosting elegido): redirección a HTTPS y HSTS.
 */
@Configuration
public class SecurityConfig {

    /** Política de contenido. Todo solo desde el propio origen (fuentes auto-alojadas). */
    static final String CONTENT_SECURITY_POLICY = "default-src 'self'; img-src 'self' data:; "
            + "style-src 'self'; script-src 'self'; font-src 'self'; frame-ancestors 'none'; "
            + "form-action 'self'; base-uri 'self'; object-src 'none'";

    static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";

    static final String LOGIN = "/admin/login";

    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(LOGIN).permitAll()
                        .requestMatchers("/admin/usuarios/**", "/admin/inscripciones/**").hasRole("ADMIN")
                        .requestMatchers("/admin", "/admin/**").hasAnyRole("ADMIN", "EDITOR")
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage(LOGIN)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin")
                        .failureUrl(LOGIN + "?error")
                        .permitAll())
                // Registro de sesiones (sin límite por persona) para poder cerrarlas al desactivar una cuenta
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl(LOGIN))
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl(LOGIN + "?salir")
                        .deleteCookies("JSESSIONID"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY))
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                PathPatternRequestMatcher.withDefaults().matcher("/admin/**"),
                                new StaticHeadersWriter("X-Robots-Tag", "noindex, nofollow"))));
        return http.build();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /** Avisa al registro cuando una sesión termina, para que no se acumulen sesiones muertas. */
    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /** BCrypt (con prefijo {bcrypt}, por si en el futuro se cambia de algoritmo). */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
