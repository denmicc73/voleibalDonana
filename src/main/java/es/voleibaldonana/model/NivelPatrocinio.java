package es.voleibaldonana.model;

/** Nivel de un patrocinador: los principales se muestran más grandes y primero. */
public enum NivelPatrocinio {

    PRINCIPAL("Principal"),
    COLABORADOR("Colaborador");

    private final String descripcion;

    NivelPatrocinio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
