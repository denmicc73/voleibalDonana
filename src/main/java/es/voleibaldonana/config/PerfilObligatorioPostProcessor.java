package es.voleibaldonana.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Impide arrancar la aplicación sin un perfil explícito ("dev", "prod" o "test").
 *
 * Así un despliegue al que se le olvida SPRING_PROFILES_ACTIVE falla con un mensaje claro
 * en lugar de arrancar con una configuración no pensada para producción.
 */
public class PerfilObligatorioPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final List<String> PERFILES_VALIDOS = List.of("dev", "prod", "test");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean hayPerfilValido = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(PERFILES_VALIDOS::contains);
        if (!hayPerfilValido) {
            throw new IllegalStateException("Falta el perfil de Spring: activa uno de " + PERFILES_VALIDOS
                    + " (por ejemplo SPRING_PROFILES_ACTIVE=prod). Sin perfil la aplicación no arranca.");
        }
    }

    /** Se ejecuta después de cargar application*.properties, para ver también los perfiles definidos allí. */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
