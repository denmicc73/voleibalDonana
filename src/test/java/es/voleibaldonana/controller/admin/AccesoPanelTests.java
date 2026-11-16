package es.voleibaldonana.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;

import es.voleibaldonana.model.Rol;
import es.voleibaldonana.model.Usuario;
import es.voleibaldonana.repository.UsuarioRepository;
import es.voleibaldonana.service.UsuarioService;

/** Fase 2: login, logout, bloqueo por intentos fallidos y portada del panel. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccesoPanelTests {

    private static final String EMAIL = "admin@test.es";
    private static final String PASSWORD = "una frase bastante larga";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @BeforeEach
    void prepararUsuario() {
        usuarios.deleteAll();
        usuarioService.crear("Ana Admin", EMAIL, PASSWORD, Rol.ADMIN);
    }

    @Test
    void loginCorrectoLlevaAlPanel() throws Exception {
        mockMvc.perform(login(EMAIL, PASSWORD))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername(EMAIL).withRoles("ADMIN"));

        Usuario usuario = usuarios.findByEmail(EMAIL).orElseThrow();
        assertThat(usuario.getUltimoAcceso()).isNotNull();
    }

    @Test
    void elEmailNoDistingueMayusculasNiEspacios() throws Exception {
        mockMvc.perform(login("  ADMIN@Test.es ", PASSWORD))
                .andExpect(authenticated().withUsername(EMAIL));
    }

    @Test
    void passwordIncorrectaVuelveAlLoginConError() throws Exception {
        mockMvc.perform(login(EMAIL, "contraseña equivocada"))
                .andExpect(redirectedUrl("/admin/login?error"))
                .andExpect(unauthenticated());

        mockMvc.perform(get("/admin/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email o contraseña incorrectos")));
    }

    @Test
    void emailInexistenteDaElMismoError() throws Exception {
        mockMvc.perform(login("nadie@test.es", PASSWORD))
                .andExpect(redirectedUrl("/admin/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void cincoFallosSeguidosBloqueanLaCuenta() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(login(EMAIL, "contraseña equivocada")).andExpect(unauthenticated());
        }

        // Bloqueada: ni siquiera con la contraseña buena
        mockMvc.perform(login(EMAIL, PASSWORD))
                .andExpect(redirectedUrl("/admin/login?error"))
                .andExpect(unauthenticated());
        assertThat(usuarios.findByEmail(EMAIL).orElseThrow().getBloqueadoHasta()).isNotNull();

        // Al desbloquearla desde el panel vuelve a entrar
        usuarioService.desbloquear(usuarios.findByEmail(EMAIL).orElseThrow().getId());
        mockMvc.perform(login(EMAIL, PASSWORD)).andExpect(authenticated());
    }

    @Test
    void unAccesoCorrectoReiniciaElContador() throws Exception {
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(login(EMAIL, "contraseña equivocada"));
        }
        mockMvc.perform(login(EMAIL, PASSWORD)).andExpect(authenticated());
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(login(EMAIL, "contraseña equivocada"));
        }
        mockMvc.perform(login(EMAIL, PASSWORD)).andExpect(authenticated());
    }

    @Test
    void cuentaDesactivadaNoEntra() throws Exception {
        Usuario usuario = usuarios.findByEmail(EMAIL).orElseThrow();
        usuarioService.cambiarActivo(usuario.getId(), false, "otra@test.es");

        mockMvc.perform(login(EMAIL, PASSWORD))
                .andExpect(redirectedUrl("/admin/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void desactivarUnaCuentaCierraSuSesionAbierta() throws Exception {
        MockHttpSession sesion = (MockHttpSession) mockMvc.perform(login(EMAIL, PASSWORD))
                .andExpect(authenticated())
                .andReturn().getRequest().getSession(false);
        mockMvc.perform(get("/admin").session(sesion)).andExpect(status().isOk());

        usuarioService.cambiarActivo(usuarios.findByEmail(EMAIL).orElseThrow().getId(), false, "otra@test.es");

        mockMvc.perform(get("/admin").session(sesion))
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void logoutCierraLaSesion() throws Exception {
        mockMvc.perform(post("/admin/logout").with(csrf()).with(user(EMAIL).roles("ADMIN")))
                .andExpect(redirectedUrl("/admin/login?salir"))
                .andExpect(unauthenticated());
    }

    @Test
    void paginaDeLoginTieneFormularioConCsrf() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(not(containsString("style=\""))));
    }

    @Test
    void conSesionIniciadaElLoginLlevaAlPanel() throws Exception {
        mockMvc.perform(get("/admin/login").with(user(EMAIL).roles("ADMIN")))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void panelDeAdminMuestraUsuariosYNombre() throws Exception {
        mockMvc.perform(get("/admin").with(user(EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Admin")))
                .andExpect(content().string(containsString("/admin/usuarios")))
                .andExpect(content().string(containsString("Cerrar sesión")));
    }

    @Test
    void panelDeEditorNoMuestraUsuariosNiInscripciones() throws Exception {
        usuarioService.crear("Eva Editora", "editora@test.es", PASSWORD, Rol.EDITOR);

        mockMvc.perform(get("/admin").with(user("editora@test.es").roles("EDITOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Eva Editora")))
                .andExpect(content().string(not(containsString("/admin/usuarios"))))
                .andExpect(content().string(not(containsString("Inscripciones"))));
    }

    private static FormLoginRequestBuilder login(
            String email, String password) {
        return formLogin("/admin/login").userParameter("email").user(email).password(password);
    }
}
