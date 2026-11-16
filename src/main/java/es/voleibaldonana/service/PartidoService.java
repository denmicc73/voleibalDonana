package es.voleibaldonana.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.PartidoForm;
import es.voleibaldonana.dto.PartidoVista;
import es.voleibaldonana.dto.ResultadoForm;
import es.voleibaldonana.dto.ResultadoForm.FilaSet;
import es.voleibaldonana.exception.NoEncontradoException;
import es.voleibaldonana.exception.ResultadoNoValidoException;
import es.voleibaldonana.model.Categoria;
import es.voleibaldonana.model.EstadoPartido;
import es.voleibaldonana.model.Partido;
import es.voleibaldonana.model.SetJugado;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.PartidoRepository;

/** Partidos y resultados set a set (ADMIN y EDITOR). */
@Service
@Transactional
public class PartidoService {

    /** Puntos máximos por set que se aceptan (evita errores de tecleo como 250). */
    static final int MAX_PUNTOS = 60;

    /** Sets que hay que ganar para acabar el partido (al mejor de 5). */
    static final int SETS_PARA_GANAR = 3;

    private final PartidoRepository partidos;
    private final CategoriaRepository categorias;

    public PartidoService(PartidoRepository partidos, CategoriaRepository categorias) {
        this.partidos = partidos;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<PartidoVista> listar(Long temporadaId, Long categoriaId) {
        List<Partido> lista = categoriaId != null
                ? partidos.findByCategoriaIdOrderByFechaHoraAsc(categoriaId)
                : partidos.findByCategoriaTemporadaIdOrderByFechaHoraAsc(temporadaId);
        return lista.stream()
                .filter(p -> p.getCategoria().getTemporada().getId().equals(temporadaId))
                .map(Vistas::partido)
                .toList();
    }

    @Transactional(readOnly = true)
    public PartidoVista vista(Long id) {
        return Vistas.partido(buscar(id));
    }

    @Transactional(readOnly = true)
    public PartidoForm formulario(Long id) {
        Partido p = buscar(id);
        PartidoForm form = new PartidoForm();
        form.setCategoriaId(p.getCategoria().getId());
        form.setGenero(p.getGenero());
        form.setRival(p.getRival());
        form.setEnCasa(p.isEnCasa());
        form.setFecha(p.getFechaHora().toLocalDate());
        form.setHora(p.getFechaHora().toLocalTime());
        form.setLugar(p.getLugar());
        form.setJornada(p.getJornada());
        form.setEstado(p.finalizado() ? EstadoPartido.PROGRAMADO : p.getEstado());
        return form;
    }

    public Long crear(PartidoForm form) {
        Partido p = new Partido(categoria(form.getCategoriaId()));
        copiar(form, p);
        p.setEstadoSinResultado(estadoSinResultado(form));
        return partidos.save(p).getId();
    }

    /** Si el partido ya tiene resultado, se corrigen los datos pero el resultado se conserva. */
    public void actualizar(Long id, PartidoForm form) {
        Partido p = buscar(id);
        p.setCategoria(categoria(form.getCategoriaId()));
        copiar(form, p);
        if (!p.finalizado()) {
            p.setEstadoSinResultado(estadoSinResultado(form));
        }
    }

    public void borrar(Long id) {
        partidos.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public ResultadoForm resultadoFormulario(Long id) {
        ResultadoForm form = new ResultadoForm();
        for (SetJugado s : buscar(id).getSets()) {
            form.getSets().set(s.getNumero() - 1, new FilaSet(s.getPuntosClub(), s.getPuntosRival()));
        }
        return form;
    }

    public void guardarResultado(Long id, ResultadoForm form) {
        Partido p = buscar(id);
        p.registrarResultado(validarSets(form.getSets()));
    }

    public void quitarResultado(Long id) {
        buscar(id).quitarResultado();
    }

    /**
     * Comprueba el resultado y lo convierte en sets. Reglas:
     * filas seguidas desde el set 1 (sin huecos), las dos casillas rellenas, 0 a 60 puntos,
     * sin empates en un set, sin empate a sets y sin sets de más cuando alguien ya ha ganado 3.
     * No se exige 25/15 puntos para admitir formatos de cantera (sets más cortos, al mejor de 3).
     */
    static List<SetJugado> validarSets(List<FilaSet> filas) {
        List<String> errores = new ArrayList<>();
        List<SetJugado> sets = new ArrayList<>();
        int ultimaConDatos = -1;
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i) != null && !filas.get(i).vacia()) {
                ultimaConDatos = i;
            }
        }
        if (ultimaConDatos < 0) {
            throw new ResultadoNoValidoException(List.of("Introduce al menos un set."));
        }
        if (ultimaConDatos >= ResultadoForm.MAX_SETS) {
            errores.add("Como máximo " + ResultadoForm.MAX_SETS + " sets.");
        }
        int ganadosClub = 0;
        int ganadosRival = 0;
        for (int i = 0; i <= Math.min(ultimaConDatos, ResultadoForm.MAX_SETS - 1); i++) {
            int numero = i + 1;
            FilaSet fila = filas.get(i);
            if (fila == null || fila.vacia()) {
                errores.add("Falta el set " + numero + ": los sets van seguidos, sin huecos.");
                continue;
            }
            if (fila.getClub() == null || fila.getRival() == null) {
                errores.add("Set " + numero + ": faltan puntos de uno de los equipos.");
                continue;
            }
            int club = fila.getClub();
            int rival = fila.getRival();
            if (club < 0 || rival < 0 || club > MAX_PUNTOS || rival > MAX_PUNTOS) {
                errores.add("Set " + numero + ": los puntos van de 0 a " + MAX_PUNTOS + ".");
                continue;
            }
            if (club == rival) {
                errores.add("Set " + numero + ": un set no puede acabar en empate.");
                continue;
            }
            if (ganadosClub == SETS_PARA_GANAR || ganadosRival == SETS_PARA_GANAR) {
                errores.add("Set " + numero + " sobra: el partido termina cuando un equipo gana "
                        + SETS_PARA_GANAR + " sets.");
                continue;
            }
            if (club > rival) {
                ganadosClub++;
            } else {
                ganadosRival++;
            }
            sets.add(new SetJugado(numero, club, rival));
        }
        if (errores.isEmpty() && ganadosClub == ganadosRival) {
            errores.add("El partido no puede acabar empatado a sets.");
        }
        if (!errores.isEmpty()) {
            throw new ResultadoNoValidoException(errores);
        }
        return sets;
    }

    private Partido buscar(Long id) {
        return partidos.findWithSetsById(id).orElseThrow(() -> new NoEncontradoException("Partido"));
    }

    private Categoria categoria(Long id) {
        return categorias.findById(id).orElseThrow(() -> new NoEncontradoException("Categoría"));
    }

    private static EstadoPartido estadoSinResultado(PartidoForm form) {
        EstadoPartido estado = form.getEstado();
        return estado == null || estado == EstadoPartido.FINALIZADO ? EstadoPartido.PROGRAMADO : estado;
    }

    private static void copiar(PartidoForm form, Partido p) {
        p.setGenero(form.getGenero());
        p.setRival(form.getRival());
        p.setEnCasa(form.getEnCasa());
        p.setFechaHora(form.getFecha().atTime(form.getHora()));
        p.setLugar(form.getLugar());
        p.setJornada(form.getJornada());
    }
}
