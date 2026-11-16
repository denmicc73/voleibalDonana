package es.voleibaldonana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Alta de una temporada nueva (solo ADMIN). */
public class NuevaTemporadaForm {

    @NotBlank(message = "Escribe la temporada")
    @Pattern(regexp = "\\d{4}/\\d{2}", message = "Usa el formato 2027/28")
    private String nombre;

    /** Copiar las categorías de la temporada activa para no empezar de cero. */
    private boolean copiarCategorias = true;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.strip();
    }

    public boolean isCopiarCategorias() {
        return copiarCategorias;
    }

    public void setCopiarCategorias(boolean copiarCategorias) {
        this.copiarCategorias = copiarCategorias;
    }
}
