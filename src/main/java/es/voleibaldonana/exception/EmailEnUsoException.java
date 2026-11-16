package es.voleibaldonana.exception;

/** Ya existe un usuario con ese email. */
public class EmailEnUsoException extends RuntimeException {

    public EmailEnUsoException() {
        super("Ya existe un usuario con ese email");
    }
}
