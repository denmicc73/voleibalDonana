package es.voleibaldonana.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Partido de un equipo del club (categoría + género) contra un rival.
 * Los sets se guardan desde el punto de vista del club; local/visitante sale de {@code enCasa}.
 */
@Entity
@Table(name = "partido")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    @Column(nullable = false, length = 100)
    private String rival;

    @Column(name = "en_casa", nullable = false)
    private boolean enCasa;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 150)
    private String lugar;

    @Column(length = 30)
    private String jornada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    @ElementCollection
    @CollectionTable(name = "set_partido", joinColumns = @JoinColumn(name = "partido_id"))
    @OrderBy("numero")
    private List<SetJugado> sets = new ArrayList<>();

    protected Partido() {
        // Para JPA
    }

    public Partido(Categoria categoria) {
        this.categoria = categoria;
    }

    /** Guarda el resultado y marca el partido como finalizado. */
    public void registrarResultado(List<SetJugado> nuevosSets) {
        sets.clear();
        sets.addAll(nuevosSets);
        estado = EstadoPartido.FINALIZADO;
    }

    /** Quita el resultado y vuelve a dejar el partido como programado. */
    public void quitarResultado() {
        sets.clear();
        estado = EstadoPartido.PROGRAMADO;
    }

    public int setsClub() {
        return (int) sets.stream().filter(SetJugado::ganadoPorElClub).count();
    }

    public int setsRival() {
        return sets.size() - setsClub();
    }

    public boolean finalizado() {
        return estado == EstadoPartido.FINALIZADO;
    }

    public Long getId() {
        return id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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
        this.rival = rival;
    }

    public boolean isEnCasa() {
        return enCasa;
    }

    public void setEnCasa(boolean enCasa) {
        this.enCasa = enCasa;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    /** Solo para estados sin resultado (programado, aplazado, suspendido). */
    public void setEstadoSinResultado(EstadoPartido nuevo) {
        if (nuevo == EstadoPartido.FINALIZADO) {
            throw new IllegalArgumentException("FINALIZADO se alcanza al registrar el resultado");
        }
        sets.clear();
        this.estado = nuevo;
    }

    public List<SetJugado> getSets() {
        return List.copyOf(sets);
    }
}
