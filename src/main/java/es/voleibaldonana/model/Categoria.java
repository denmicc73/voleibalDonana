package es.voleibaldonana.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Categoría de juego de una temporada (Sénior, Júnior, Juvenil, Cadete, Infantil, Alevín...).
 * Los años de nacimiento cambian cada temporada; {@code anioDesde} nulo significa "y antes".
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "temporada_id", nullable = false)
    private Temporada temporada;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion_edad", length = 50)
    private String descripcionEdad;

    @Column(name = "anio_desde")
    private Integer anioDesde;

    @Column(name = "anio_hasta")
    private Integer anioHasta;

    @Column(length = 100)
    private String horario;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean visible = true;

    protected Categoria() {
        // Para JPA
    }

    public Categoria(Temporada temporada) {
        this.temporada = temporada;
    }

    /** Copia de esta categoría para otra temporada (los años se ajustan después). */
    public Categoria copiarPara(Temporada otra) {
        Categoria copia = new Categoria(otra);
        copia.nombre = nombre;
        copia.descripcionEdad = descripcionEdad;
        copia.anioDesde = anioDesde;
        copia.anioHasta = anioHasta;
        copia.horario = horario;
        copia.orden = orden;
        copia.visible = visible;
        return copia;
    }

    public Long getId() {
        return id;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcionEdad() {
        return descripcionEdad;
    }

    public void setDescripcionEdad(String descripcionEdad) {
        this.descripcionEdad = descripcionEdad;
    }

    public Integer getAnioDesde() {
        return anioDesde;
    }

    public void setAnioDesde(Integer anioDesde) {
        this.anioDesde = anioDesde;
    }

    public Integer getAnioHasta() {
        return anioHasta;
    }

    public void setAnioHasta(Integer anioHasta) {
        this.anioHasta = anioHasta;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
