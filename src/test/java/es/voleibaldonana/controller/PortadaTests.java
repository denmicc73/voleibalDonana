package es.voleibaldonana.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import es.voleibaldonana.dto.PartidoForm;
import es.voleibaldonana.dto.ResultadoForm;
import es.voleibaldonana.dto.ResultadoForm.FilaSet;
import es.voleibaldonana.model.Categoria;
import es.voleibaldonana.model.Genero;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.PartidoRepository;
import es.voleibaldonana.repository.TemporadaRepository;
import es.voleibaldonana.service.PartidoService;

/** Fase 3: la portada muestra los datos reales de la temporada activa. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortadaTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private PartidoRepository partidos;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private TemporadaRepository temporadas;

    @BeforeEach
    @AfterEach
    void limpiar() {
        partidos.deleteAll();
        categorias.findAll().forEach(c -> {
            c.setVisible(true);
            categorias.save(c);
        });
    }

    @Test
    void sinPartidosAvisaDeQueLaTemporadaEstaEnPreparacion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Temporada en preparación")))
                .andExpect(content().string(containsString("Temporada 2026 / 27")))
                .andExpect(content().string(containsString("Nacidos en 2011 y 2012")))
                .andExpect(content().string(not(containsString("Último resultado"))));
    }

    @Test
    void muestraElUltimoResultadoSetASetYElProximoPartido() throws Exception {
        Categoria cadete = categoria("Cadete");
        Long jugado = crear(cadete, Genero.FEMENINO, "CD Pasado", true, LocalDate.now().minusDays(3));
        ResultadoForm resultado = new ResultadoForm();
        resultado.getSets().set(0, new FilaSet(25, 20));
        resultado.getSets().set(1, new FilaSet(23, 25));
        resultado.getSets().set(2, new FilaSet(25, 18));
        resultado.getSets().set(3, new FilaSet(25, 22));
        partidoService.guardarResultado(jugado, resultado);
        crear(cadete, Genero.MASCULINO, "CD Futuro", false, LocalDate.now().plusDays(4));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Último resultado")))
                .andExpect(content().string(containsString("Cadete Fem.")))
                .andExpect(content().string(containsString("3 — 1")))
                .andExpect(content().string(containsString("¡Victoria!")))
                .andExpect(content().string(containsString("25-20 · 23-25 · 25-18 · 25-22")))
                .andExpect(content().string(containsString("Próximo partido")))
                .andExpect(content().string(containsString("CD Futuro  /  Almonte Doñana")))
                .andExpect(content().string(containsString("Campo rival")));
    }

    @Test
    void lasCategoriasOcultasNoSalenEnLaPortada() throws Exception {
        Categoria alevin = categoria("Alevín");
        crear(alevin, Genero.MIXTO, "CD Oculto", true, LocalDate.now().plusDays(2));
        alevin.setVisible(false);
        categorias.save(alevin);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Alevín"))))
                .andExpect(content().string(not(containsString("CD Oculto"))));
    }

    private Categoria categoria(String nombre) {
        Long temporada = temporadas.findFirstByActivaTrue().orElseThrow().getId();
        return categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(temporada).stream()
                .filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Long crear(Categoria categoria, Genero genero, String rival, boolean enCasa, LocalDate fecha) {
        PartidoForm form = new PartidoForm();
        form.setCategoriaId(categoria.getId());
        form.setGenero(genero);
        form.setRival(rival);
        form.setEnCasa(enCasa);
        form.setFecha(fecha);
        form.setHora(LocalTime.of(12, 0));
        return partidoService.crear(form);
    }
}
