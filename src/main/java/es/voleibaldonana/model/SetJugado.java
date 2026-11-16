package es.voleibaldonana.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Un set de un partido, con los puntos del club y del rival. */
@Embeddable
public class SetJugado {

    @Column(nullable = false)
    private int numero;

    @Column(name = "puntos_club", nullable = false)
    private int puntosClub;

    @Column(name = "puntos_rival", nullable = false)
    private int puntosRival;

    protected SetJugado() {
        // Para JPA
    }

    public SetJugado(int numero, int puntosClub, int puntosRival) {
        this.numero = numero;
        this.puntosClub = puntosClub;
        this.puntosRival = puntosRival;
    }

    public boolean ganadoPorElClub() {
        return puntosClub > puntosRival;
    }

    public int getNumero() {
        return numero;
    }

    public int getPuntosClub() {
        return puntosClub;
    }

    public int getPuntosRival() {
        return puntosRival;
    }
}
