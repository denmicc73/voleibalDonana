package es.voleibaldonana.service;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Cierra las sesiones abiertas de una persona (por ejemplo, al desactivar su cuenta),
 * para que no siga dentro del panel hasta que caduque la sesión.
 */
@Service
public class SesionesService {

    private final SessionRegistry sessionRegistry;

    public SesionesService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void cerrarSesiones(String email) {
        String emailNormalizado = UsuarioService.normalizarEmail(email);
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> p instanceof UserDetails ud && ud.getUsername().equals(emailNormalizado))
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .forEach(sesion -> sesion.expireNow());
    }
}
