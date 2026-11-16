package es.voleibaldonana.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/** Imagen del logo de un patrocinador (mismo id que el patrocinador). */
@Entity
@Table(name = "patrocinador_logo")
public class LogoPatrocinador {

    @Id
    @Column(name = "patrocinador_id")
    private Long patrocinadorId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] datos;

    protected LogoPatrocinador() {
        // Para JPA
    }

    public LogoPatrocinador(Long patrocinadorId, byte[] datos) {
        this.patrocinadorId = patrocinadorId;
        this.datos = datos;
    }

    public Long getPatrocinadorId() {
        return patrocinadorId;
    }

    public byte[] getDatos() {
        return datos;
    }

    public void setDatos(byte[] datos) {
        this.datos = datos;
    }
}
