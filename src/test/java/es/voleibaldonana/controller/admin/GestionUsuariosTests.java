package es.voleibaldonana.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import es.voleibaldonana.model.Rol;
import es.voleibaldonana.model.Usuario;
import es.voleibaldonana.repository.UsuarioRepository;
import es.voleibaldonana.service.UsuarioService;

/** Fase 2: alta y activación de usuarios (solo ADMIN) y cambio de la contraseña propia. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GestionUsuariosTests {

    private static final String ADMIN = "admin@test.es";
    private static final String PASSWORD = "una frase bastante larga";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararAdmin() {
        usuarios.deleteAll();
        usuarioService.crear("Ana Admin", ADMIN, PASSWORD, Rol.ADMIN);
    }

    @Test
    void listadoMuestraLosUsuarios() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(ADMIN)))
                .andExpect(content().string(containsString("Administración")))
                .andExpect(content().string(not(containsString("{bcrypt}"))));
    }

    @Test
    void adminCreaUnEditor() throws Exception {
        mockMvc.perform(post("/admin/usuarios").with(csrf()).with(comoAdmin())
                        .param("nombre", "Eva Editora")
                        .param("email", " Eva@Test.es ")
                        .param("rol", "EDITOR")
                        .param("password", "otra frase muy larga"))
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attributeExists("aviso"));

        Usuario creado = usuarios.findByEmail("eva@test.es").orElseThrow();
        assertThat(creado.getRol()).isEqualTo(Rol.EDITOR);
        assertThat(creado.getPasswordHash()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("otra frase muy larga", creado.getPasswordHash())).isTrue();
    }

    @Test
    void emailRepetidoMuestraError() throws Exception {
        mockMvc.perform(post("/admin/usuarios").with(csrf()).with(comoAdmin())
                        .param("nombre", "Otra")
                        .param("email", "ADMIN@test.es")
                        .param("rol", "EDITOR")
                        .param("password", "otra frase muy larga"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe un usuario con ese email")))
                // La contraseña enviada no se devuelve en la página
                .andExpect(content().string(not(containsString("otra frase muy larga"))));
    }

    @Test
    void passwordCortaMuestraError() throws Exception {
        mockMvc.perform(post("/admin/usuarios").with(csrf()).with(comoAdmin())
                        .param("nombre", "Otra")
                        .param("email", "otra@test.es")
                        .param("rol", "EDITOR")
                        .param("password", "corta"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("entre 12 y 72 caracteres")));
        assertThat(usuarios.existsByEmail("otra@test.es")).isFalse();
    }

    @Test
    void editorNoPuedeCrearUsuarios() throws Exception {
        mockMvc.perform(post("/admin/usuarios").with(csrf()).with(user("editor@test.es").roles("EDITOR"))
                        .param("nombre", "Intrusa")
                        .param("email", "intrusa@test.es")
                        .param("rol", "ADMIN")
                        .param("password", "otra frase muy larga"))
                .andExpect(status().isForbidden());
        assertThat(usuarios.existsByEmail("intrusa@test.es")).isFalse();
    }

    @Test
    void nadiePuedeDesactivarseASiMismo() throws Exception {
        Long id = usuarios.findByEmail(ADMIN).orElseThrow().getId();

        mockMvc.perform(post("/admin/usuarios/{id}/desactivar", id).with(csrf()).with(comoAdmin()))
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attribute("error", "No puedes desactivar tu propia cuenta."));
        assertThat(usuarios.findById(id).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void adminDesactivaYActivaAOtro() throws Exception {
        Long id = usuarioService.crear("Eva Editora", "eva@test.es", PASSWORD, Rol.EDITOR);

        mockMvc.perform(post("/admin/usuarios/{id}/desactivar", id).with(csrf()).with(comoAdmin()))
                .andExpect(redirectedUrl("/admin/usuarios"));
        assertThat(usuarios.findById(id).orElseThrow().isActivo()).isFalse();

        mockMvc.perform(post("/admin/usuarios/{id}/activar", id).with(csrf()).with(comoAdmin()))
                .andExpect(redirectedUrl("/admin/usuarios"));
        assertThat(usuarios.findById(id).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void usuarioInexistenteDa404() throws Exception {
        mockMvc.perform(post("/admin/usuarios/{id}/activar", 999_999).with(csrf()).with(comoAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminRestableceLaPasswordDeOtro() throws Exception {
        Long id = usuarioService.crear("Eva Editora", "eva@test.es", PASSWORD, Rol.EDITOR);

        mockMvc.perform(get("/admin/usuarios/{id}/password", id).with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("eva@test.es")));

        mockMvc.perform(post("/admin/usuarios/{id}/password", id).with(csrf()).with(comoAdmin())
                        .param("password", "temporal bastante larga"))
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attributeExists("aviso"));

        String hash = usuarios.findById(id).orElseThrow().getPasswordHash();
        assertThat(passwordEncoder.matches("temporal bastante larga", hash)).isTrue();
    }

    @Test
    void restablecerPasswordCortaMuestraError() throws Exception {
        Long id = usuarioService.crear("Eva Editora", "eva@test.es", PASSWORD, Rol.EDITOR);

        mockMvc.perform(post("/admin/usuarios/{id}/password", id).with(csrf()).with(comoAdmin())
                        .param("password", "corta"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("entre 12 y 72 caracteres")));

        String hash = usuarios.findById(id).orElseThrow().getPasswordHash();
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
    }

    @Test
    void laPasswordPropiaNoSeRestableceDesdeUsuarios() throws Exception {
        Long id = usuarios.findByEmail(ADMIN).orElseThrow().getId();

        mockMvc.perform(post("/admin/usuarios/{id}/password", id).with(csrf()).with(comoAdmin())
                        .param("password", "temporal bastante larga"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mi cuenta")));

        String hash = usuarios.findById(id).orElseThrow().getPasswordHash();
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
    }

    @Test
    void editorNoPuedeRestablecerPasswords() throws Exception {
        Long id = usuarios.findByEmail(ADMIN).orElseThrow().getId();

        mockMvc.perform(post("/admin/usuarios/{id}/password", id).with(csrf())
                        .with(user("editor@test.es").roles("EDITOR"))
                        .param("password", "temporal bastante larga"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambioDePasswordCorrecto() throws Exception {
        mockMvc.perform(post("/admin/cuenta").with(csrf()).with(comoAdmin())
                        .param("actual", PASSWORD)
                        .param("nueva", "nueva frase todavía más larga")
                        .param("repetida", "nueva frase todavía más larga"))
                .andExpect(redirectedUrl("/admin/cuenta"))
                .andExpect(flash().attributeExists("aviso"));

        String hash = usuarios.findByEmail(ADMIN).orElseThrow().getPasswordHash();
        assertThat(passwordEncoder.matches("nueva frase todavía más larga", hash)).isTrue();
    }

    @Test
    void cambioDePasswordConActualIncorrecta() throws Exception {
        mockMvc.perform(post("/admin/cuenta").with(csrf()).with(comoAdmin())
                        .param("actual", "no es la mía para nada")
                        .param("nueva", "nueva frase todavía más larga")
                        .param("repetida", "nueva frase todavía más larga"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La contraseña actual no es correcta")))
                .andExpect(content().string(not(containsString("nueva frase todavía más larga"))));

        String hash = usuarios.findByEmail(ADMIN).orElseThrow().getPasswordHash();
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
    }

    @Test
    void cambioDePasswordQueNoCoincide() throws Exception {
        mockMvc.perform(post("/admin/cuenta").with(csrf()).with(comoAdmin())
                        .param("actual", PASSWORD)
                        .param("nueva", "nueva frase todavía más larga")
                        .param("repetida", "otra frase diferente del todo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no coinciden")));
    }

    private static RequestPostProcessor comoAdmin() {
        return user(ADMIN).roles("ADMIN");
    }
}
