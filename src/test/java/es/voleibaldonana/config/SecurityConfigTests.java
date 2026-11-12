package es.voleibaldonana.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Comprueba la seguridad provisional de la Fase 0: rutas, CSRF y cabeceras. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void inicioEsPublico() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEstaBloqueado() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/cualquier")).andExpect(status().isForbidden());
    }

    @Test
    void postSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/"))
                .andExpect(status().isForbidden());
    }

    @Test
    void respuestaIncluyeCabecerasDeSeguridad() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(header().string("Content-Security-Policy", SecurityConfig.CONTENT_SECURITY_POLICY))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy", SecurityConfig.PERMISSIONS_POLICY));
    }
}
