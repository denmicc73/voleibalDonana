package es.voleibaldonana.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.support.TransactionTemplate;

import es.voleibaldonana.model.EstadoPartido;
import es.voleibaldonana.model.Partido;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.PartidoRepository;
import es.voleibaldonana.repository.TemporadaRepository;

/** Fase 3: gestión de partidos y resultados desde el panel (EDITOR puede, sin tocar temporadas). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PartidosAdminTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartidoRepository partidos;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private TemporadaRepository temporadas;

    @Autowired
    private TransactionTemplate transaccion;

    private Long cadeteId;

    @BeforeEach
    void preparar() {
        partidos.deleteAll();
        Long temporada = temporadas.findFirstByActivaTrue().orElseThrow().getId();
        cadeteId = categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(temporada).stream()
                .filter(c -> c.getNombre().equals("Cadete")).findFirst().orElseThrow().getId();
    }

    @Test
    void editorCreaUnPartido() throws Exception {
        crearPartido();

        assertThat(partidos.findAll()).singleElement().satisfies(p -> {
            assertThat(p.getRival()).isEqualTo("CD Prueba");
            assertThat(p.getEstado()).isEqualTo(EstadoPartido.PROGRAMADO);
            assertThat(p.getFechaHora()).hasToString("2026-12-05T10:30");
        });
    }

    @Test
    void partidoSinDatosObligatoriosMuestraErrores() throws Exception {
        mockMvc.perform(post("/admin/partidos").with(csrf()).with(comoEditor())
                        .param("rival", " "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Escribe el rival")))
                .andExpect(content().string(containsString("Elige la categoría")))
                .andExpect(content().string(containsString("Indica la fecha")));
        assertThat(partidos.count()).isZero();
    }

    @Test
    void editorGuardaElResultadoSetASet() throws Exception {
        Long id = crearPartido();

        mockMvc.perform(post("/admin/partidos/{id}/resultado", id).with(csrf()).with(comoEditor())
                        .param("sets[0].club", "25").param("sets[0].rival", "20")
                        .param("sets[1].club", "25").param("sets[1].rival", "18")
                        .param("sets[2].club", "25").param("sets[2].rival", "23"))
                .andExpect(status().is3xxRedirection());

        Partido partido = conSets(id);
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.FINALIZADO);
        assertThat(partido.setsClub()).isEqualTo(3);
        assertThat(partido.getSets()).hasSize(3);
    }

    @Test
    void resultadoNoValidoMuestraLosErroresYNoSeGuarda() throws Exception {
        Long id = crearPartido();

        mockMvc.perform(post("/admin/partidos/{id}/resultado", id).with(csrf()).with(comoEditor())
                        .param("sets[0].club", "25").param("sets[0].rival", "25"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("un set no puede acabar en empate")));

        assertThat(conSets(id).getEstado()).isEqualTo(EstadoPartido.PROGRAMADO);
    }

    @Test
    void puntosQueNoSonNumerosMuestranError() throws Exception {
        Long id = crearPartido();

        mockMvc.perform(post("/admin/partidos/{id}/resultado", id).with(csrf()).with(comoEditor())
                        .param("sets[0].club", "abc").param("sets[0].rival", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tienen que ser números")));
    }

    @Test
    void editarUnPartidoConResultadoLoConserva() throws Exception {
        Long id = crearPartido();
        mockMvc.perform(post("/admin/partidos/{id}/resultado", id).with(csrf()).with(comoEditor())
                .param("sets[0].club", "25").param("sets[0].rival", "20")
                .param("sets[1].club", "25").param("sets[1].rival", "18")
                .param("sets[2].club", "25").param("sets[2].rival", "23"));

        mockMvc.perform(post("/admin/partidos/{id}", id).with(csrf()).with(comoEditor())
                        .param("categoriaId", cadeteId.toString()).param("genero", "FEMENINO")
                        .param("rival", "CD Prueba Corregido").param("enCasa", "true")
                        .param("fecha", "2026-12-05").param("hora", "11:00").param("estado", "APLAZADO"))
                .andExpect(status().is3xxRedirection());

        Partido partido = conSets(id);
        assertThat(partido.getRival()).isEqualTo("CD Prueba Corregido");
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.FINALIZADO);
        assertThat(partido.getSets()).hasSize(3);
    }

    @Test
    void quitarResultadoVuelveAProgramado() throws Exception {
        Long id = crearPartido();
        mockMvc.perform(post("/admin/partidos/{id}/resultado", id).with(csrf()).with(comoEditor())
                .param("sets[0].club", "25").param("sets[0].rival", "20")
                .param("sets[1].club", "25").param("sets[1].rival", "18")
                .param("sets[2].club", "25").param("sets[2].rival", "23"));

        mockMvc.perform(post("/admin/partidos/{id}/resultado/quitar", id).with(csrf()).with(comoEditor()))
                .andExpect(status().is3xxRedirection());

        Partido partido = conSets(id);
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.PROGRAMADO);
        assertThat(partido.getSets()).isEmpty();
    }

    @Test
    void borrarPartido() throws Exception {
        Long id = crearPartido();

        mockMvc.perform(get("/admin/partidos/{id}/borrar", id).with(comoEditor()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("¿Borrar este partido?")));
        mockMvc.perform(post("/admin/partidos/{id}/borrar", id).with(csrf()).with(comoEditor()))
                .andExpect(status().is3xxRedirection());

        assertThat(partidos.existsById(id)).isFalse();
    }

    @Test
    void listadoMuestraLosPartidos() throws Exception {
        crearPartido();

        mockMvc.perform(get("/admin/partidos").with(comoEditor()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Almonte Doñana — CD Prueba")))
                .andExpect(content().string(containsString("Programado")));
    }

    @Test
    void partidoInexistenteDa404() throws Exception {
        mockMvc.perform(get("/admin/partidos/{id}/resultado", 999_999).with(comoEditor()))
                .andExpect(status().isNotFound());
    }

    @Test
    void editorNoEntraEnTemporadas() throws Exception {
        mockMvc.perform(get("/admin/temporadas").with(comoEditor()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/temporadas").with(csrf()).with(comoEditor()).param("nombre", "2030/31"))
                .andExpect(status().isForbidden());
    }

    private Long crearPartido() throws Exception {
        mockMvc.perform(post("/admin/partidos").with(csrf()).with(comoEditor())
                        .param("categoriaId", cadeteId.toString())
                        .param("genero", "FEMENINO")
                        .param("rival", "CD Prueba")
                        .param("enCasa", "true")
                        .param("fecha", "2026-12-05")
                        .param("hora", "10:30")
                        .param("estado", "PROGRAMADO"))
                .andExpect(status().is3xxRedirection());
        return partidos.findAll().getLast().getId();
    }

    /** Carga el partido con sus sets dentro de una transacción (los sets son perezosos). */
    private Partido conSets(Long id) {
        return transaccion.execute(estado -> {
            Partido p = partidos.findById(id).orElseThrow();
            p.getSets();
            return p;
        });
    }

    private static RequestPostProcessor comoEditor() {
        return user("editor@test.es").roles("EDITOR");
    }
}
