package es.voleibaldonana.service;

import java.util.List;

import es.voleibaldonana.dto.CategoriaFila;
import es.voleibaldonana.dto.PartidoVista;
import es.voleibaldonana.model.Categoria;
import es.voleibaldonana.model.Club;
import es.voleibaldonana.model.Partido;
import es.voleibaldonana.model.SetJugado;

/** Conversión de entidades a DTO de vista (se usa dentro de transacciones). */
final class Vistas {

    private Vistas() {
    }

    static CategoriaFila categoria(Categoria c) {
        return new CategoriaFila(c.getId(), c.getNombre(), c.getDescripcionEdad(),
                nacidos(c.getAnioDesde(), c.getAnioHasta()), c.getHorario(), c.getOrden(), c.isVisible());
    }

    /** "Nacidos en 2011 y 2012", "Nacidos en 2006 y antes", "Nacidos entre 2010 y 2013"... */
    static String nacidos(Integer desde, Integer hasta) {
        if (desde == null && hasta == null) {
            return null;
        }
        if (desde == null) {
            return "Nacidos en " + hasta + " y antes";
        }
        if (hasta == null) {
            return "Nacidos en " + desde + " y después";
        }
        if (desde.equals(hasta)) {
            return "Nacidos en " + desde;
        }
        if (hasta == desde + 1) {
            return "Nacidos en " + desde + " y " + hasta;
        }
        return "Nacidos entre " + desde + " y " + hasta;
    }

    static PartidoVista partido(Partido p) {
        boolean casa = p.isEnCasa();
        String local = casa ? Club.NOMBRE : p.getRival();
        String visitante = casa ? p.getRival() : Club.NOMBRE;
        String tanteo = null;
        Boolean victoria = null;
        List<String> sets = List.of();
        if (p.finalizado()) {
            int club = p.setsClub();
            int rival = p.setsRival();
            tanteo = casa ? club + " – " + rival : rival + " – " + club;
            victoria = club > rival;
            sets = p.getSets().stream().map(s -> puntos(s, casa)).toList();
        }
        return new PartidoVista(p.getId(), p.getCategoria().getId(), p.getCategoria().getNombre(), p.getGenero(),
                local, visitante, casa, p.getFechaHora(), FormatoFechas.diaMes(p.getFechaHora()),
                FormatoFechas.hora(p.getFechaHora()), p.getLugar(), p.getJornada(), p.getEstado(), tanteo, sets,
                victoria);
    }

    private static String puntos(SetJugado s, boolean casa) {
        return casa ? s.getPuntosClub() + "-" + s.getPuntosRival() : s.getPuntosRival() + "-" + s.getPuntosClub();
    }
}
