package es.voleibaldonana.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import es.voleibaldonana.model.EstadoPartido;
import es.voleibaldonana.model.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta o edición de un partido (ADMIN y EDITOR). El resultado va aparte (ResultadoForm).
 * {@code estado} solo admite PROGRAMADO, APLAZADO o SUSPENDIDO; si el partido ya tiene
 * resultado, se conserva y el estado no cambia.
 */
public class PartidoForm {

    @NotNull(message = "Elige la categoría")
    private Long categoriaId;

    @NotNull(message = "Elige el equipo")
    private Genero genero;

    @NotBlank(message = "Escribe el rival")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String rival;

    @NotNull(message = "Indica si se juega en casa o fuera")
    private Boolean enCasa;

    @NotNull(message = "Indica la fecha")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Indica la hora")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @Size(max = 150, message = "Máximo 150 caracteres")
    private String lugar;

    @Size(max = 30, message = "Máximo 30 caracteres")
    private String jornada;

    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getRival() {
        return rival;
    }

    public void setRival(String rival) {
        this.rival = limpiar(rival);
    }

    public Boolean getEnCasa() {
        return enCasa;
    }

    public void setEnCasa(Boolean enCasa) {
        this.enCasa = enCasa;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = limpiar(lugar);
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = limpiar(jornada);
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartido estado) {
        this.estado = estado;
    }
}
