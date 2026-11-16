package es.voleibaldonana.dto;

import es.voleibaldonana.model.NivelPatrocinio;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Alta o edición de un patrocinador (solo ADMIN). El logo llega aparte, como archivo. */
public class PatrocinadorForm {

    @NotBlank(message = "Escribe el nombre")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    /** Solo http(s): nada de "javascript:" ni otros esquemas en el enlace. */
    @Size(max = 255, message = "Máximo 255 caracteres")
    @Pattern(regexp = "https?://[^\\s<>\"']+", message = "Escribe la dirección completa, empezando por https://")
    private String web;

    @NotNull(message = "Elige el nivel")
    private NivelPatrocinio nivel = NivelPatrocinio.COLABORADOR;

    @Min(value = 0, message = "Entre 0 y 99")
    @Max(value = 99, message = "Entre 0 y 99")
    private int orden;

    private boolean visible = true;

    /** En la edición: quitar el logo actual sin subir otro. */
    private boolean quitarLogo;

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = limpiar(nombre);
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = limpiar(web);
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

    public boolean isQuitarLogo() {
        return quitarLogo;
    }

    public void setQuitarLogo(boolean quitarLogo) {
        this.quitarLogo = quitarLogo;
    }
}
