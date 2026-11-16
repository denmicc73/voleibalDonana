package es.voleibaldonana.exception;

/** Operación que las reglas del club no permiten (el mensaje se muestra a quien la intenta). */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
