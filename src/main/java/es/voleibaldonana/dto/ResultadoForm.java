package es.voleibaldonana.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado set a set: hasta 5 filas con los puntos del club y del rival.
 * Las filas vacías del final se ignoran. Las reglas se comprueban en PartidoService.
 */
public class ResultadoForm {

    public static final int MAX_SETS = 5;

    private List<FilaSet> sets = new ArrayList<>();

    public ResultadoForm() {
        for (int i = 0; i < MAX_SETS; i++) {
            sets.add(new FilaSet());
        }
    }

    public List<FilaSet> getSets() {
        return sets;
    }

    public void setSets(List<FilaSet> sets) {
        this.sets = sets;
    }

    /** Una fila del formulario. Vacía = ese set no se jugó. */
    public static class FilaSet {

        private Integer club;
        private Integer rival;

        public FilaSet() {
        }

        public FilaSet(Integer club, Integer rival) {
            this.club = club;
            this.rival = rival;
        }

        public boolean vacia() {
            return club == null && rival == null;
        }

        public Integer getClub() {
            return club;
        }

        public void setClub(Integer club) {
            this.club = club;
        }

        public Integer getRival() {
            return rival;
        }

        public void setRival(Integer rival) {
            this.rival = rival;
        }
    }
}
