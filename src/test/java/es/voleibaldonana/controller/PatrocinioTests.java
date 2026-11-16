package es.voleibaldonana.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import es.voleibaldonana.model.Patrocinador;
import es.voleibaldonana.repository.LogoPatrocinadorRepository;
import es.voleibaldonana.repository.PatrocinadorRepository;
import es.voleibaldonana.repository.SolicitudPatrocinioRepository;
import es.voleibaldonana.service.LimiteEnviosService;

/** Fase 4: patrocinadores en la portada, formulario "Patrocina al club" y gestión en el panel. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatrocinioTests {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatrocinadorRepository patrocinadores;

    @Autowired
    private LogoPatrocinadorRepository logos;

    @Autowired
    private SolicitudPatrocinioRepository solicitudes;

    @Autowired
    private LimiteEnviosService limite;

    @BeforeEach
    void limpiar() {
        logos.deleteAll();
        patrocinadores.deleteAll();
        solicitudes.deleteAll();
        limite.reiniciar();
    }

    // --- Formulario público ---

    @Test
    void paginaPatrocinaTieneFormularioConCsrfYCampoTrampa() throws Exception {
        mockMvc.perform(get("/patrocina"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Patrocina al club")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("name=\"sitio\"")))
                .andExpect(content().string(not(containsString("style=\""))));
    }

    @Test
    void solicitudValidaSeGuardaYLlevaALaPaginaDeGracias() throws Exception {
        mockMvc.perform(solicitud().param("empresa", "Bar Prueba"))
                .andExpect(redirectedUrl("/patrocina/gracias"));

        assertThat(solicitudes.findAll()).singleElement().satisfies(s -> {
            assertThat(s.getEmpresa()).isEqualTo("Bar Prueba");
            assertThat(s.getEmail()).isEqualTo("ana@ejemplo.es");
            assertThat(s.isAtendida()).isFalse();
        });
        mockMvc.perform(get("/patrocina/gracias")).andExpect(content().string(containsString("Solicitud recibida")));
    }

    @Test
    void solicitudSinDatosObligatoriosNoSeGuarda() throws Exception {
        mockMvc.perform(post("/patrocina").with(csrf()).param("email", "no-es-un-email"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Escribe el nombre de la empresa o comercio")))
                .andExpect(content().string(containsString("El correo electrónico no es válido")))
                .andExpect(content().string(containsString("Tienes que aceptar la política de privacidad")));
        assertThat(solicitudes.count()).isZero();
    }

    @Test
    void siUnBotRellenaElCampoTrampaNoSeGuardaPeroNoSeLeAvisa() throws Exception {
        mockMvc.perform(solicitud().param("empresa", "Spam SA").param("sitio", "https://spam.example"))
                .andExpect(redirectedUrl("/patrocina/gracias"));
        assertThat(solicitudes.count()).isZero();
    }

    @Test
    void demasiadasSolicitudesSeguidasDesdeLaMismaIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(solicitud().param("empresa", "Empresa " + i)).andExpect(redirectedUrl("/patrocina/gracias"));
        }
        mockMvc.perform(solicitud().param("empresa", "Una más"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Has enviado varias solicitudes seguidas")));
        assertThat(solicitudes.count()).isEqualTo(5);
    }

    @Test
    void solicitudSinCsrfSeRechaza() throws Exception {
        mockMvc.perform(post("/patrocina").param("empresa", "X").param("contacto", "Y")
                        .param("email", "a@b.es").param("privacidad", "true"))
                .andExpect(status().isForbidden());
    }

    // --- Portada y logos ---

    @Test
    void sinPatrocinadoresLaPortadaMuestraLosHuecos() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("Tu marca")))
                .andExpect(content().string(containsString("href=\"/patrocina\"")));
    }

    @Test
    void laPortadaMuestraLosPatrocinadoresVisiblesConLogoYEnlace() throws Exception {
        crearPatrocinador("Panadería Visible", "https://panaderia.example", true, PNG);
        crearPatrocinador("Taller Oculto", null, false, null);

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("alt=\"Panadería Visible\"")))
                .andExpect(content().string(containsString("href=\"https://panaderia.example\"")))
                .andExpect(content().string(containsString("rel=\"sponsored noopener\"")))
                .andExpect(content().string(not(containsString("Taller Oculto"))))
                .andExpect(content().string(not(containsString("Tu marca"))));
    }

    @Test
    void elLogoSeSirveConSuTipoYCache() throws Exception {
        crearPatrocinador("Con Logo", null, true, PNG);
        Patrocinador p = patrocinadores.findAll().getFirst();

        mockMvc.perform(get("/patrocinadores/{id}/logo", p.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Cache-Control", containsString("max-age")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void logoInexistenteDa404() throws Exception {
        crearPatrocinador("Sin Logo", null, true, null);
        Patrocinador p = patrocinadores.findAll().getFirst();

        mockMvc.perform(get("/patrocinadores/{id}/logo", p.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/patrocinadores/{id}/logo", 999_999)).andExpect(status().isNotFound());
    }

    // --- Panel ---

    @Test
    void adminRechazaUnSvgDisfrazadoDePng() throws Exception {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(multipart("/admin/patrocinadores")
                        .file(new MockMultipartFile("logo", "logo.png", "image/png", svg))
                        .param("nombre", "Malo").param("nivel", "COLABORADOR").param("orden", "0")
                        .with(csrf()).with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PNG, JPG o WebP")));
        assertThat(patrocinadores.count()).isZero();
    }

    @Test
    void webConEsquemaPeligrosoSeRechaza() throws Exception {
        mockMvc.perform(multipart("/admin/patrocinadores")
                        .param("nombre", "Raro").param("web", "javascript:alert(1)")
                        .param("nivel", "COLABORADOR").param("orden", "0")
                        .with(csrf()).with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("empezando por https://")));
        assertThat(patrocinadores.count()).isZero();
    }

    @Test
    void adminQuitaElLogo() throws Exception {
        crearPatrocinador("Con Logo", null, true, PNG);
        Long id = patrocinadores.findAll().getFirst().getId();

        mockMvc.perform(multipart("/admin/patrocinadores/{id}", id)
                        .param("nombre", "Con Logo").param("nivel", "COLABORADOR").param("orden", "0")
                        .param("visible", "true").param("quitarLogo", "true")
                        .with(csrf()).with(comoAdmin()))
                .andExpect(redirectedUrl("/admin/patrocinadores"));

        assertThat(patrocinadores.findById(id).orElseThrow().tieneLogo()).isFalse();
        assertThat(logos.existsById(id)).isFalse();
    }

    @Test
    void adminGestionaLasSolicitudes() throws Exception {
        mockMvc.perform(solicitud().param("empresa", "Bar Prueba"));
        Long id = solicitudes.findAll().getFirst().getId();

        mockMvc.perform(get("/admin").with(comoAdmin()))
                .andExpect(content().string(containsString("1 solicitud pendiente")));
        mockMvc.perform(get("/admin/solicitudes-patrocinio").with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bar Prueba")))
                .andExpect(content().string(containsString("mailto:ana@ejemplo.es")));

        mockMvc.perform(post("/admin/solicitudes-patrocinio/{id}/atendida", id).with(csrf()).with(comoAdmin()))
                .andExpect(redirectedUrl("/admin/solicitudes-patrocinio"));
        assertThat(solicitudes.findById(id).orElseThrow().isAtendida()).isTrue();

        mockMvc.perform(post("/admin/solicitudes-patrocinio/{id}/borrar", id).with(csrf()).with(comoAdmin()));
        assertThat(solicitudes.existsById(id)).isFalse();
    }

    @Test
    void editorNoEntraEnPatrocinio() throws Exception {
        RequestPostProcessor editor = user("editor@test.es").roles("EDITOR");
        mockMvc.perform(get("/admin/patrocinadores").with(editor)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/solicitudes-patrocinio").with(editor)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin").with(editor))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/admin/patrocinadores"))));
    }

    private void crearPatrocinador(String nombre, String web, boolean visible, byte[] logo) throws Exception {
        var peticion = multipart("/admin/patrocinadores")
                .param("nombre", nombre).param("nivel", "PRINCIPAL").param("orden", "0")
                .param("_visible", "on");
        if (visible) {
            peticion.param("visible", "true");
        }
        if (web != null) {
            peticion.param("web", web);
        }
        if (logo != null) {
            peticion.file(new MockMultipartFile("logo", "logo.png", "image/png", logo));
        }
        mockMvc.perform(peticion.with(csrf()).with(comoAdmin())).andExpect(redirectedUrl("/admin/patrocinadores"));
    }

    private static MockHttpServletRequestBuilder solicitud() {
        return post("/patrocina").with(csrf())
                .param("contacto", "Ana").param("email", "ana@ejemplo.es")
                .param("privacidad", "true").param("mensaje", "Nos interesa");
    }

    private static RequestPostProcessor comoAdmin() {
        return user("admin@test.es").roles("ADMIN");
    }
}
