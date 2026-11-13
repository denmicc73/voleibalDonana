package es.voleibaldonana.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Fase 1: la portada y las páginas legales responden y muestran lo esperado. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaginasPublicasTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void portadaMuestraElNombreDelClub() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Almonte Doñana")))
                .andExpect(content().string(containsString("/css/estilos.css")))
                // El formulario de maqueta lleva el token CSRF gracias a th:action
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                // Nada de estilos en línea (la CSP no los permite)
                .andExpect(content().string(not(containsString("style=\""))));
    }

    @Test
    void avisoLegalEsBorrador() throws Exception {
        comprobarPaginaLegal("/aviso-legal", "legal/aviso-legal");
    }

    @Test
    void privacidadEsBorrador() throws Exception {
        comprobarPaginaLegal("/privacidad", "legal/privacidad");
    }

    @Test
    void cookiesEsBorrador() throws Exception {
        comprobarPaginaLegal("/cookies", "legal/cookies");
    }

    private void comprobarPaginaLegal(String ruta, String vista) throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(view().name(vista))
                .andExpect(content().string(containsString("Borrador — pendiente de revisión por el club")));
    }
}
