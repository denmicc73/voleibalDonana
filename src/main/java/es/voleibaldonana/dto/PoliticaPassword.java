package es.voleibaldonana.dto;

/** Reglas de longitud de las contraseñas del panel. */
public final class PoliticaPassword {

    /** Longitud mínima: mejor una frase larga que símbolos raros. */
    public static final int MIN = 12;

    /** BCrypt solo admite 72 bytes; se limita para no fallar ni truncar sin avisar. */
    public static final int MAX = 72;

    public static final String MENSAJE = "La contraseña debe tener entre " + MIN + " y " + MAX + " caracteres";

    private PoliticaPassword() {
    }

    /** Comprueba la longitud (en bytes UTF-8 para el máximo, que es lo que cuenta BCrypt). */
    public static boolean valida(String password) {
        return password != null
                && password.length() >= MIN
                && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= MAX;
    }
}
