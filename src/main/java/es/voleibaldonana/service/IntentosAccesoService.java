package es.voleibaldonana.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.config.AccesoProperties;
import es.voleibaldonana.repository.UsuarioRepository;

/**
 * Protección frente a fuerza bruta: escucha los eventos de login de Spring Security.
 * Tras {@code club.acceso.max-intentos} contraseñas erróneas seguidas, la cuenta queda bloqueada
 * durante {@code club.acceso.bloqueo}. Un acceso correcto pone el contador a cero.
 */
@Service
public class IntentosAccesoService {

    private static final Logger log = LoggerFactory.getLogger(IntentosAccesoService.class);

    private final UsuarioRepository usuarios;
    private final AccesoProperties propiedades;
    private final Clock clock;

    public IntentosAccesoService(UsuarioRepository usuarios, AccesoProperties propiedades, Clock clock) {
        this.usuarios = usuarios;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        usuarios.findByEmail(UsuarioService.normalizarEmail(evento.getAuthentication().getName()))
                .ifPresent(usuario -> {
                    usuario.registrarFallo(propiedades.maxIntentos(), ahora.plus(propiedades.bloqueo()));
                    if (usuario.estaBloqueado(ahora)) {
                        log.warn("Cuenta {} bloqueada {} por intentos fallidos", usuario.getId(),
                                propiedades.bloqueo());
                    }
                });
    }

    @EventListener
    @Transactional
    public void alAcertar(AuthenticationSuccessEvent evento) {
        usuarios.findByEmail(UsuarioService.normalizarEmail(evento.getAuthentication().getName()))
                .ifPresent(usuario -> usuario.registrarAcceso(LocalDateTime.now(clock)));
    }
}
