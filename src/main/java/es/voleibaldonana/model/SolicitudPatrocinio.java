package es.voleibaldonana.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Solicitud enviada desde el formulario "Patrocina al club". */
@Entity
@Table(name = "solicitud_patrocinio")
public class SolicitudPatrocinio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String empresa;

    @Column(nullable = false, length = 100)
    private String contacto;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime recibida;

    @Column(nullable = false)
    private boolean atendida;

    protected SolicitudPatrocinio() {
        // Para JPA
    }

    public SolicitudPatrocinio(String empresa, String contacto, String email, String telefono, String mensaje,
            LocalDateTime recibida) {
        this.empresa = empresa;
        this.contacto = contacto;
        this.email = email;
        this.telefono = telefono;
        this.mensaje = mensaje;
        this.recibida = recibida;
    }

    public Long getId() {
        return id;
    }

    public String getEmpresa() {
        return empresa;
    }

    public String getContacto() {
        return contacto;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getRecibida() {
        return recibida;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }
}
