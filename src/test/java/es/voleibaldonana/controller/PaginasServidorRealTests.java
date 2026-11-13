package es.voleibaldonana.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pruebas con un servidor real (puerto aleatorio), para lo que MockMvc no reproduce:
 * - el reenvío interno a /error que renderiza error/404.html;
 * - el envío real de la respuesta (una página con formulario debe llegar entera).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaginasServidorRealTests {

    @LocalServerPort
    private int puerto;

    @Test
    void rutaInexistenteMuestraPagina404Propia() throws Exception {
        HttpResponse<String> respuesta = pedir("/no-existe");

        assertThat(respuesta.statusCode()).isEqualTo(404);
        assertThat(respuesta.body())
                .contains("Fuera de la pista")
                .contains("Almonte Doñana");
    }

    @Test
    void portadaLlegaCompletaConElFormulario() throws Exception {
        HttpResponse<String> respuesta = pedir("/");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body())
                .contains("name=\"_csrf\"")
                .contains("Aviso legal")
                .contains("</html>");
    }

    private HttpResponse<String> pedir(String ruta) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Accept", "text/html")
                .GET()
                .build();
        try (HttpClient cliente = HttpClient.newHttpClient()) {
            return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
        }
    }
}
