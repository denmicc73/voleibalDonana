package es.voleibaldonana.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Patrocinador del club. El logo se guarda aparte (LogoPatrocinador); aquí solo su tipo y una
 * versión que cambia con cada subida, para que el navegador no muestre uno viejo de la caché.
 */
@Entity
@Table(name = "patrocinador")
public class Patrocinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String web;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelPatrocinio nivel = NivelPatrocinio.COLABORADOR;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean visible = true;

    @Column(name = "logo_tipo", length = 30)
    private String logoTipo;

    @Column(name = "logo_version")
    private Long logoVersion;

    public Patrocinador() {
    }

    public boolean tieneLogo() {
        return logoTipo != null;
    }

    public void registrarLogo(String tipo, long version) {
        this.logoTipo = tipo;
        this.logoVersion = version;
    }

    public void quitarLogo() {
        this.logoTipo = null;
        this.logoVersion = null;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public NivelPatrocinio getNivel() {
        return nivel;
    }

    public void setNivel(NivelPatrocinio nivel) {
        this.nivel = nivel;
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

    public String getLogoTipo() {
        return logoTipo;
    }

    public Long getLogoVersion() {
        return logoVersion;
    }
}
