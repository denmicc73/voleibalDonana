package es.voleibaldonana.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** No existe lo pedido (temporada, categoría o partido): responde 404. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String que) {
        super(que + " no encontrado");
    }
}
