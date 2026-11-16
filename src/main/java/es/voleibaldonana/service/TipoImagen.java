package es.voleibaldonana.service;

import java.util.Optional;

/**
 * Reconoce PNG, JPEG y WebP por sus primeros bytes (no se fía del nombre ni del tipo que
 * dice el navegador). SVG no se admite: puede llevar scripts.
 */
public final class TipoImagen {

    /** Tamaño máximo de un logo. */
    public static final int MAX_BYTES = 1024 * 1024;

    private TipoImagen() {
    }

    public static Optional<String> detectar(byte[] datos) {
        if (datos == null || datos.length < 12) {
            return Optional.empty();
        }
        if (empiezaPor(datos, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of("image/png");
        }
        if (empiezaPor(datos, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of("image/jpeg");
        }
        if (empiezaPor(datos, 0, 'R', 'I', 'F', 'F') && empiezaPor(datos, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of("image/webp");
        }
        return Optional.empty();
    }

    private static boolean empiezaPor(byte[] datos, int desde, int... esperados) {
        for (int i = 0; i < esperados.length; i++) {
            if ((datos[desde + i] & 0xFF) != esperados[i]) {
                return false;
            }
        }
        return true;
    }
}
