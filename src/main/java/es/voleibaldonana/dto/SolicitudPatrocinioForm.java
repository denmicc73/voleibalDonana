package es.voleibaldonana.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulario público "Patrocina al club".
 * {@code sitio} es un campo trampa: está oculto y una persona no lo rellena; si llega con texto,
 * es un bot y la solicitud se descarta sin avisar.
 */
public class SolicitudPatrocinioForm {

    @NotBlank(message = "Escribe el nombre de la empresa o comercio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String empresa;

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String contacto;

    @NotBlank(message = "Escribe un correo electrónico")
    @Email(message = "El correo electrónico no es válido")
    @Size(max = 254, message = "Máximo 254 caracteres")
    private String email;

    @Size(max = 30, message = "Máximo 30 caracteres")
    @Pattern(regexp = "[0-9 +()-]*", message = "Solo números, espacios y +")
    private String telefono;

    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String mensaje;

    @AssertTrue(message = "Tienes que aceptar la política de privacidad")
    private boolean privacidad;

    private String sitio;

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    public boolean esBot() {
        return sitio != null && !sitio.isBlank();
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = limpiar(empresa);
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = limpiar(contacto);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = limpiar(email);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = limpiar(telefono);
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = limpiar(mensaje);
    }

    public boolean isPrivacidad() {
        return privacidad;
    }

    public void setPrivacidad(boolean privacidad) {
        this.privacidad = privacidad;
    }

    public String getSitio() {
        return sitio;
    }

    public void setSitio(String sitio) {
        this.sitio = sitio;
    }
}
