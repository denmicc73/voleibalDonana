package es.voleibaldonana.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Seguridad: rutas públicas y del panel, roles, CSRF y cabeceras. */
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
    void adminSinSesionLlevaAlLogin() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
        mockMvc.perform(get("/admin/cualquier")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void loginEsPublicoYNoSeIndexa() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Robots-Tag", "noindex, nofollow"));
    }

    @Test
    void editorNoEntraEnUsuariosNiInscripciones() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user("editor@test.es").roles("EDITOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/inscripciones").with(user("editor@test.es").roles("EDITOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorEntraEnLaPortadaDelPanel() throws Exception {
        mockMvc.perform(get("/admin").with(user("editor@test.es").roles("EDITOR")))
                .andExpect(status().isOk());
    }

    @Test
    void adminEntraEnUsuarios() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user("admin@test.es").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void logoutSinCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/admin/logout").with(user("admin@test.es").roles("ADMIN")))
                .andExpect(status().isForbidden());
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
