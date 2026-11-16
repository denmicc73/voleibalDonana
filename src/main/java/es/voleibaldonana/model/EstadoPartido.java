package es.voleibaldonana.model;

/** Estado de un partido. FINALIZADO solo se alcanza al guardar el resultado set a set. */
public enum EstadoPartido {

    PROGRAMADO("Programado"),
    FINALIZADO("Final"),
    APLAZADO("Aplazado"),
    SUSPENDIDO("Suspendido");

    private final String descripcion;

    EstadoPartido(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
