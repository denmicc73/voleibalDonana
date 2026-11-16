package es.voleibaldonana.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import es.voleibaldonana.exception.OperacionNoPermitidaException;

/** Reconocimiento de imágenes por su contenido y límites del logo. */
class TipoImagenTests {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 1};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0x24, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

    @Test
    void reconocePngJpegYWebp() {
        assertThat(TipoImagen.detectar(PNG)).contains("image/png");
        assertThat(TipoImagen.detectar(JPEG)).contains("image/jpeg");
        assertThat(TipoImagen.detectar(WEBP)).contains("image/webp");
    }

    @Test
    void rechazaSvgGifYArchivosCortos() {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);
        byte[] gif = "GIF89a......".getBytes(StandardCharsets.US_ASCII);
        assertThat(TipoImagen.detectar(svg)).isEmpty();
        assertThat(TipoImagen.detectar(gif)).isEmpty();
        assertThat(TipoImagen.detectar(new byte[] {(byte) 0x89, 'P'})).isEmpty();
        assertThat(TipoImagen.detectar(null)).isEmpty();
    }

    @Test
    void logoVacioEsQueNoSeSubeNinguno() {
        assertThat(PatrocinadorService.validarLogo(null)).isNull();
        assertThat(PatrocinadorService.validarLogo(new byte[0])).isNull();
    }

    @Test
    void logoDeMasDeUnMegaSeRechaza() {
        byte[] grande = new byte[TipoImagen.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);
        assertThatThrownBy(() -> PatrocinadorService.validarLogo(grande))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("1 MB");
    }

    @Test
    void logoQueNoEsImagenSeRechaza() {
        assertThatThrownBy(() -> PatrocinadorService.validarLogo("hola, no soy una imagen".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("PNG, JPG o WebP");
    }
}
