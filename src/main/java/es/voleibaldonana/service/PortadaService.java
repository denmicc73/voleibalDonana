package es.voleibaldonana.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.CategoriaFila;
import es.voleibaldonana.dto.PartidoVista;
import es.voleibaldonana.dto.Portada;
import es.voleibaldonana.dto.Portada.CalendarioCategoria;
import es.voleibaldonana.dto.Portada.ProximoPartido;
import es.voleibaldonana.dto.Portada.UltimoResultado;
import es.voleibaldonana.model.EstadoPartido;
import es.voleibaldonana.model.Temporada;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.PartidoRepository;
import es.voleibaldonana.repository.TemporadaRepository;

/** Reúne los datos de la portada desde la temporada activa (solo categorías visibles). */
@Service
@Transactional(readOnly = true)
public class PortadaService {

    /** En cada pestaña del calendario: los últimos partidos jugados y los próximos. */
    static final int PARTIDOS_PASADOS = 3;
    static final int PARTIDOS_FUTUROS = 5;

    private final TemporadaRepository temporadas;
    private final CategoriaRepository categorias;
    private final PartidoRepository partidos;
    private final Clock clock;

    public PortadaService(TemporadaRepository temporadas, CategoriaRepository categorias,
            PartidoRepository partidos, Clock clock) {
        this.temporadas = temporadas;
        this.categorias = categorias;
        this.partidos = partidos;
        this.clock = clock;
    }

    public Portada datos() {
        List<Integer> anios = aniosNacimiento();
        Optional<Temporada> activa = temporadas.findFirstByActivaTrue();
        if (activa.isEmpty()) {
            return new Portada(null, null, null, List.of(), List.of(), anios);
        }
        Temporada temporada = activa.get();
        LocalDateTime ahora = LocalDateTime.now(clock);

        List<CategoriaFila> visibles = categorias
                .findByTemporadaIdAndVisibleTrueOrderByOrdenAscNombreAsc(temporada.getId()).stream()
                .map(Vistas::categoria).toList();
        Set<Long> idsVisibles = visibles.stream().map(CategoriaFila::id).collect(Collectors.toSet());

        List<PartidoVista> todos = partidos.findByCategoriaTemporadaIdOrderByFechaHoraAsc(temporada.getId()).stream()
                .filter(p -> idsVisibles.contains(p.getCategoria().getId()))
                .map(Vistas::partido)
                .toList();

        Map<Long, List<PartidoVista>> porCategoria = todos.stream()
                .collect(Collectors.groupingBy(PartidoVista::categoriaId));
        List<CalendarioCategoria> calendario = visibles.stream()
                .map(c -> new CalendarioCategoria(c, recortar(porCategoria.getOrDefault(c.id(), List.of()), ahora)))
                .toList();

        return new Portada("Temporada " + temporada.getNombre().replace("/", " / "),
                ultimoResultado(todos), proximoPartido(todos, ahora), visibles, calendario, anios);
    }

    /** Años de nacimiento para el formulario de inscripción: de 6 a 70 años, del más reciente al más antiguo. */
    public List<Integer> aniosNacimiento() {
        int actual = Year.now(clock).getValue();
        return IntStream.iterate(actual - 6, anio -> anio >= actual - 70, anio -> anio - 1).boxed().toList();
    }

    private static UltimoResultado ultimoResultado(List<PartidoVista> todos) {
        return todos.stream()
                .filter(PartidoVista::finalizado)
                .max(Comparator.comparing(PartidoVista::fechaHora))
                .map(p -> new UltimoResultado(
                        p.jornada() == null ? p.equipo() : p.equipo() + " · " + p.jornada(),
                        p.local(), p.visitante(), p.tanteo().replace("–", "—"), p.setsTexto(),
                        Boolean.TRUE.equals(p.victoria()),
                        FormatoFechas.larga(p.fechaHora()) + " · " + (p.enCasa() ? "En casa" : "Fuera")))
                .orElse(null);
    }

    private static ProximoPartido proximoPartido(List<PartidoVista> todos, LocalDateTime ahora) {
        return todos.stream()
                .filter(p -> p.estado() == EstadoPartido.PROGRAMADO && !p.fechaHora().isBefore(ahora))
                .findFirst()
                .map(p -> new ProximoPartido(p.equipo(), p.local(), p.visitante(),
                        FormatoFechas.corta(p.fechaHora()) + " · " + FormatoFechas.hora(p.fechaHora()) + " h · "
                                + (p.lugar() != null ? p.lugar() : (p.enCasa() ? "En casa" : "Campo rival"))))
                .orElse(null);
    }

    /** Los últimos partidos ya jugados (o pasados) y los próximos, en orden de fecha. */
    private static List<PartidoVista> recortar(List<PartidoVista> lista, LocalDateTime ahora) {
        List<PartidoVista> pasados = lista.stream().filter(p -> p.fechaHora().isBefore(ahora)).toList();
        List<PartidoVista> futuros = lista.stream().filter(p -> !p.fechaHora().isBefore(ahora)).toList();
        return Stream.concat(
                        pasados.stream().skip(Math.max(0, pasados.size() - PARTIDOS_PASADOS)),
                        futuros.stream().limit(PARTIDOS_FUTUROS))
                .toList();
    }
}
