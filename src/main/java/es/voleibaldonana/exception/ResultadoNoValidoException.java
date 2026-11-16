package es.voleibaldonana.exception;

import java.util.List;

/** El resultado set a set no cumple las reglas; lleva todos los errores para mostrarlos juntos. */
public class ResultadoNoValidoException extends RuntimeException {

    private final List<String> errores;

    public ResultadoNoValidoException(List<String> errores) {
        super(String.join(" ", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
