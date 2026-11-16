package es.voleibaldonana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contraseña temporal que un ADMIN pone a otra persona que ha olvidado la suya. */
public class RestablecerPasswordForm {

    @NotBlank(message = "Escribe una contraseña temporal")
    @Size(min = PoliticaPassword.MIN, max = PoliticaPassword.MAX, message = PoliticaPassword.MENSAJE)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
