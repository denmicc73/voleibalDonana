package es.voleibaldonana.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Administrador que se crea al arrancar si todavía no hay ninguno.
 * Se rellena con las variables de entorno ADMIN_EMAIL, ADMIN_PASSWORD y ADMIN_NOMBRE
 * (ver application.properties). Nunca se escriben en el repositorio.
 */
@ConfigurationProperties("club.admin")
public record AdminInicialProperties(
        String email,
        String password,
        @DefaultValue("Administración") String nombre) {

    public boolean configurado() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }

    /** Evita que la contraseña aparezca en logs si alguien imprime el objeto. */
    @Override
    public String toString() {
        return "AdminInicialProperties[email=" + email + ", nombre=" + nombre + "]";
    }
}
