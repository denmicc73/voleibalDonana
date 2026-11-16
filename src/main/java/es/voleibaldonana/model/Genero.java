package es.voleibaldonana.model;

/** Género del equipo que juega un partido (el club es femenino y masculino). */
public enum Genero {

    FEMENINO("Femenino", "Fem."),
    MASCULINO("Masculino", "Masc."),
    MIXTO("Mixto", "Mixto");

    private final String descripcion;
    private final String corto;

    Genero(String descripcion, String corto) {
        this.descripcion = descripcion;
        this.corto = corto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCorto() {
        return corto;
    }
}
