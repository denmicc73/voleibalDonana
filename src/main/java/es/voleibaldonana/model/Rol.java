package es.voleibaldonana.model;

/** Roles del panel de administración. */
public enum Rol {

    /** Acceso completo, incluidos usuarios e inscripciones (datos de menores). */
    ADMIN("Administración"),

    /** Solo resultados y calendario; sin acceso a datos personales. */
    EDITOR("Edición de resultados");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
