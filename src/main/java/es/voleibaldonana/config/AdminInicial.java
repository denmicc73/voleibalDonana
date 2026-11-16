package es.voleibaldonana.config;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.Rol;
import es.voleibaldonana.service.UsuarioService;

/**
 * Crea el primer administrador al arrancar, solo si todavía no existe ninguno.
 *
 * - Con ADMIN_EMAIL y ADMIN_PASSWORD definidas: lo crea con esos datos. Después conviene cambiar la
 *   contraseña desde "Mi cuenta"; las variables ya no se vuelven a usar mientras exista un administrador.
 * - Sin ellas, en el perfil dev: crea admin@dev.local con una contraseña aleatoria y la muestra en el log.
 * - Sin ellas, en otro perfil: solo avisa en el log (nadie podrá entrar al panel).
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    static final String EMAIL_DEV = "admin@dev.local";

    private final UsuarioService usuarioService;
    private final AdminInicialProperties propiedades;
    private final Environment entorno;

    public AdminInicial(UsuarioService usuarioService, AdminInicialProperties propiedades, Environment entorno) {
        this.usuarioService = usuarioService;
        this.propiedades = propiedades;
        this.entorno = entorno;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioService.hayAdministrador()) {
            return;
        }
        if (propiedades.configurado()) {
            try {
                usuarioService.crear(propiedades.nombre(), propiedades.email(), propiedades.password(), Rol.ADMIN);
            } catch (OperacionNoPermitidaException e) {
                throw new IllegalStateException("ADMIN_PASSWORD no es válida: " + e.getMessage(), e);
            }
            log.info("Creado el administrador inicial ({}). Cambia su contraseña desde 'Mi cuenta'.",
                    UsuarioService.normalizarEmail(propiedades.email()));
            return;
        }
        if (entorno.acceptsProfiles(Profiles.of("dev"))) {
            String password = passwordAleatoria();
            usuarioService.crear("Administración (dev)", EMAIL_DEV, password, Rol.ADMIN);
            log.warn("[SOLO DEV] Administrador de prueba: {} / {}", EMAIL_DEV, password);
            return;
        }
        log.warn("No hay ningún administrador: define ADMIN_EMAIL y ADMIN_PASSWORD y reinicia la aplicación.");
    }

    private static String passwordAleatoria() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
