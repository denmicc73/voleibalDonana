package es.voleibaldonana.exception;

/** La contraseña actual indicada no es correcta. */
public class PasswordIncorrectaException extends RuntimeException {

    public PasswordIncorrectaException() {
        super("La contraseña actual no es correcta");
    }
}
