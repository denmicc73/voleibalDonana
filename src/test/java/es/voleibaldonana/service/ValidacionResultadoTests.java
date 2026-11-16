package es.voleibaldonana.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.voleibaldonana.dto.ResultadoForm.FilaSet;
import es.voleibaldonana.exception.ResultadoNoValidoException;
import es.voleibaldonana.model.SetJugado;

/** Reglas del resultado set a set (PartidoService.validarSets). */
class ValidacionResultadoTests {

    @Test
    void victoriaTresAUno() {
        List<SetJugado> sets = PartidoService.validarSets(filas(25, 20, 23, 25, 25, 18, 25, 22));

        assertThat(sets).hasSize(4);
        assertThat(sets).extracting(SetJugado::getNumero).containsExactly(1, 2, 3, 4);
        assertThat(sets.stream().filter(SetJugado::ganadoPorElClub).count()).isEqualTo(3);
    }

    @Test
    void derrotaEnElQuintoSet() {
        assertThat(PartidoService.validarSets(filas(25, 23, 18, 25, 25, 20, 21, 25, 13, 15))).hasSize(5);
    }

    @Test
    void admiteAlMejorDeTresParaLaCantera() {
        assertThat(PartidoService.validarSets(filas(15, 10, 12, 15, 15, 13))).hasSize(3);
    }

    @Test
    void sinSetsDaError() {
        assertErrores(filas(), "Introduce al menos un set.");
    }

    @Test
    void unSetNoPuedeAcabarEnEmpate() {
        assertErrores(filas(25, 25, 25, 20, 25, 20), "Set 1: un set no puede acabar en empate.");
    }

    @Test
    void faltanPuntosDeUnEquipo() {
        List<FilaSet> filas = filas(25, 20, 25, 20);
        filas.add(new FilaSet(25, null));
        assertErrores(filas, "Set 3: faltan puntos de uno de los equipos.");
    }

    @Test
    void losSetsVanSeguidos() {
        List<FilaSet> filas = filas(25, 20);
        filas.add(new FilaSet());
        filas.add(new FilaSet(25, 20));
        filas.add(new FilaSet(25, 20));
        assertErrores(filas, "Falta el set 2: los sets van seguidos, sin huecos.");
    }

    @Test
    void puntosFueraDeRango() {
        assertErrores(filas(250, 20, 25, 20, 25, 20), "Set 1: los puntos van de 0 a 60.");
    }

    @Test
    void sobranSetsTrasGanarTres() {
        assertErrores(filas(25, 20, 25, 20, 25, 20, 25, 20), "Set 4 sobra: el partido termina cuando un equipo gana 3 sets.");
    }

    @Test
    void noPuedeAcabarEmpatadoASets() {
        assertErrores(filas(25, 20, 20, 25), "El partido no puede acabar empatado a sets.");
    }

    @Test
    void comoMaximoCincoSets() {
        List<FilaSet> filas = filas(25, 20, 20, 25, 25, 20, 20, 25, 15, 13);
        filas.add(new FilaSet(25, 20));
        assertThatThrownBy(() -> PartidoService.validarSets(filas))
                .isInstanceOf(ResultadoNoValidoException.class)
                .hasMessageContaining("Como máximo 5 sets.");
    }

    @Test
    void lasFilasVaciasDelFinalSeIgnoran() {
        List<FilaSet> filas = filas(25, 10, 25, 10, 25, 10);
        filas.add(new FilaSet());
        filas.add(new FilaSet());
        assertThat(PartidoService.validarSets(filas)).hasSize(3);
    }

    private static void assertErrores(List<FilaSet> filas, String... esperados) {
        assertThatThrownBy(() -> PartidoService.validarSets(filas))
                .isInstanceOfSatisfying(ResultadoNoValidoException.class,
                        e -> assertThat(e.getErrores()).containsExactly(esperados));
    }

    /** Pares de puntos (club, rival). */
    private static List<FilaSet> filas(int... puntos) {
        List<FilaSet> filas = new ArrayList<>();
        for (int i = 0; i < puntos.length; i += 2) {
            filas.add(new FilaSet(puntos[i], puntos[i + 1]));
        }
        return filas;
    }
}
