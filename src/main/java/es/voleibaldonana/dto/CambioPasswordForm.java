package es.voleibaldonana.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulario "Mi cuenta": cambio de la contraseña propia. */
public class CambioPasswordForm {

    @NotBlank(message = "Escribe tu contraseña actual")
    private String actual;

    @NotBlank(message = "Escribe la nueva contraseña")
    @Size(min = PoliticaPassword.MIN, max = PoliticaPassword.MAX, message = PoliticaPassword.MENSAJE)
    private String nueva;

    @NotBlank(message = "Repite la nueva contraseña")
    private String repetida;

    @AssertTrue(message = "Las dos contraseñas nuevas no coinciden")
    public boolean isCoinciden() {
        return nueva == null || nueva.equals(repetida);
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }

    public String getRepetida() {
        return repetida;
    }

    public void setRepetida(String repetida) {
        this.repetida = repetida;
    }
}
