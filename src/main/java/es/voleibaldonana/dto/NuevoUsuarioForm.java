package es.voleibaldonana.dto;

import es.voleibaldonana.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alta de una persona en el panel (solo ADMIN). */
public class NuevoUsuarioForm {

    @NotBlank(message = "Escribe el nombre")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "Escribe el email")
    @Email(message = "El email no es válido")
    @Size(max = 254, message = "Máximo 254 caracteres")
    private String email;

    @NotNull(message = "Elige un rol")
    private Rol rol = Rol.EDITOR;

    @NotBlank(message = "Escribe una contraseña inicial")
    @Size(min = PoliticaPassword.MIN, max = PoliticaPassword.MAX, message = PoliticaPassword.MENSAJE)
    private String password;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.strip();
    }

    public String getEmail() {
        return email;
    }

    /** Sin espacios alrededor, para que no falle la validación por un espacio al copiar y pegar. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.strip();
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
