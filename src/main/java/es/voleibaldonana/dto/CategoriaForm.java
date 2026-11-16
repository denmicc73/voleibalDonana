package es.voleibaldonana.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta o edición de una categoría de una temporada (solo ADMIN). */
public class CategoriaForm {

    @NotBlank(message = "Escribe el nombre")
    @Size(max = 50, message = "Máximo 50 caracteres")
    private String nombre;

    @Size(max = 50, message = "Máximo 50 caracteres")
    private String descripcionEdad;

    @Min(value = 1900, message = "Año no válido")
    @Max(value = 2100, message = "Año no válido")
    private Integer anioDesde;

    @Min(value = 1900, message = "Año no válido")
    @Max(value = 2100, message = "Año no válido")
    private Integer anioHasta;

    @Size(max = 100, message = "Máximo 100 caracteres")
    private String horario;

    @Min(value = 0, message = "Entre 0 y 99")
    @Max(value = 99, message = "Entre 0 y 99")
    private int orden;

    private boolean visible = true;

    @AssertTrue(message = "El primer año no puede ser posterior al último")
    public boolean isAniosEnOrden() {
        return anioDesde == null || anioHasta == null || anioDesde <= anioHasta;
    }

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = limpiar(nombre);
    }

    public String getDescripcionEdad() {
        return descripcionEdad;
    }

    public void setDescripcionEdad(String descripcionEdad) {
        this.descripcionEdad = limpiar(descripcionEdad);
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
        this.horario = limpiar(horario);
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
