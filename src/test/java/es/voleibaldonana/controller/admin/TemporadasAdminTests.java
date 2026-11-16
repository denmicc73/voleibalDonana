package es.voleibaldonana.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import es.voleibaldonana.dto.PartidoForm;
import es.voleibaldonana.model.Genero;
import es.voleibaldonana.model.Temporada;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.PartidoRepository;
import es.voleibaldonana.repository.TemporadaRepository;
import es.voleibaldonana.service.PartidoService;

/** Fase 3: temporadas y categorías (solo ADMIN). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemporadasAdminTests {

    private static final String INICIAL = "2026/27";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemporadaRepository temporadas;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private PartidoRepository partidos;

    @Autowired
    private PartidoService partidoService;

    /** Deja la base como la crea Flyway: solo la temporada inicial, activa, con sus 6 categorías. */
    @AfterEach
    void restaurar() {
        partidos.deleteAll();
        temporadas.findAll().forEach(t -> {
            t.setActiva(t.getNombre().equals(INICIAL));
            temporadas.save(t);
        });
        temporadas.findAll().stream().filter(t -> !t.getNombre().equals(INICIAL)).forEach(t -> {
            categorias.deleteAll(categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(t.getId()));
            temporadas.delete(t);
        });
    }

    @Test
    void listadoMuestraLaTemporadaActiva() throws Exception {
        mockMvc.perform(get("/admin/temporadas").with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(INICIAL)))
                .andExpect(content().string(containsString("Activa · se ve en la web")));
    }

    @Test
    void crearTemporadaCopiandoCategorias() throws Exception {
        mockMvc.perform(post("/admin/temporadas").with(csrf()).with(comoAdmin())
                        .param("nombre", "2027/28").param("copiarCategorias", "true"))
                .andExpect(status().is3xxRedirection());

        Temporada nueva = buscar("2027/28");
        assertThat(nueva.isActiva()).isFalse();
        assertThat(categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(nueva.getId())).hasSize(6);
    }

    @Test
    void crearTemporadaSinCategorias() throws Exception {
        // Casilla desmarcada: el navegador solo envía el campo oculto "_copiarCategorias" de Spring
        mockMvc.perform(post("/admin/temporadas").with(csrf()).with(comoAdmin())
                        .param("nombre", "2027/28").param("_copiarCategorias", "on"))
                .andExpect(status().is3xxRedirection());

        assertThat(categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(buscar("2027/28").getId())).isEmpty();
    }

    @Test
    void nombreConFormatoIncorrectoODuplicado() throws Exception {
        mockMvc.perform(post("/admin/temporadas").with(csrf()).with(comoAdmin()).param("nombre", "2027"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usa el formato 2027/28")));
        mockMvc.perform(post("/admin/temporadas").with(csrf()).with(comoAdmin()).param("nombre", INICIAL))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe la temporada")));
    }

    @Test
    void activarUnaTemporadaDesactivaLasDemas() throws Exception {
        mockMvc.perform(post("/admin/temporadas").with(csrf()).with(comoAdmin()).param("nombre", "2027/28"));
        Long id = buscar("2027/28").getId();

        mockMvc.perform(post("/admin/temporadas/{id}/activar", id).with(csrf()).with(comoAdmin()))
                .andExpect(status().is3xxRedirection());

        assertThat(buscar("2027/28").isActiva()).isTrue();
        assertThat(buscar(INICIAL).isActiva()).isFalse();
    }

    @Test
    void editarCategoriaYValidarAnios() throws Exception {
        Long id = categoriaInicial("Cadete");

        mockMvc.perform(post("/admin/temporadas/categorias/{id}", id).with(csrf()).with(comoAdmin())
                        .param("nombre", "Cadete").param("anioDesde", "2013").param("anioHasta", "2012")
                        .param("orden", "4").param("visible", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no puede ser posterior")));

        mockMvc.perform(post("/admin/temporadas/categorias/{id}", id).with(csrf()).with(comoAdmin())
                        .param("nombre", "Cadete").param("descripcionEdad", "14–15 años")
                        .param("anioDesde", "2011").param("anioHasta", "2012")
                        .param("horario", "Lun. y mié. · 18:30 h").param("orden", "4").param("visible", "true"))
                .andExpect(status().is3xxRedirection());

        assertThat(categorias.findById(id).orElseThrow().getHorario()).isEqualTo("Lun. y mié. · 18:30 h");
    }

    @Test
    void categoriaConPartidosNoSeBorra() throws Exception {
        Long id = categoriaInicial("Infantil");
        PartidoForm form = new PartidoForm();
        form.setCategoriaId(id);
        form.setGenero(Genero.MIXTO);
        form.setRival("CD Prueba");
        form.setEnCasa(true);
        form.setFecha(LocalDate.of(2026, 12, 1));
        form.setHora(LocalTime.NOON);
        partidoService.crear(form);

        mockMvc.perform(post("/admin/temporadas/categorias/{id}/borrar", id).with(csrf()).with(comoAdmin()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("tiene partidos")));
        assertThat(categorias.existsById(id)).isTrue();
    }

    @Test
    void crearYBorrarCategoriaSinPartidos() throws Exception {
        Long temporada = buscar(INICIAL).getId();
        mockMvc.perform(post("/admin/temporadas/{id}/categorias", temporada).with(csrf()).with(comoAdmin())
                        .param("nombre", "Benjamín").param("orden", "7").param("visible", "true"))
                .andExpect(status().is3xxRedirection());
        Long id = categoriaInicial("Benjamín");

        mockMvc.perform(post("/admin/temporadas/categorias/{id}/borrar", id).with(csrf()).with(comoAdmin()))
                .andExpect(flash().attributeExists("aviso"));
        assertThat(categorias.existsById(id)).isFalse();
    }

    private Temporada buscar(String nombre) {
        return temporadas.findAll().stream().filter(t -> t.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Long categoriaInicial(String nombre) {
        return categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(buscar(INICIAL).getId()).stream()
                .filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow().getId();
    }

    private static RequestPostProcessor comoAdmin() {
        return user("admin@test.es").roles("ADMIN");
    }
}
