package es.voleibaldonana.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.NuevaTemporadaForm;
import es.voleibaldonana.dto.TemporadaFila;
import es.voleibaldonana.exception.NoEncontradoException;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.Temporada;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.TemporadaRepository;

/** Temporadas: solo una está activa, y es la que se muestra en la web. */
@Service
@Transactional
public class TemporadaService {

    private final TemporadaRepository temporadas;
    private final CategoriaRepository categorias;

    public TemporadaService(TemporadaRepository temporadas, CategoriaRepository categorias) {
        this.temporadas = temporadas;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public Optional<TemporadaFila> activa() {
        return temporadas.findFirstByActivaTrue().map(TemporadaService::fila);
    }

    @Transactional(readOnly = true)
    public List<TemporadaFila> listar() {
        return temporadas.findAllByOrderByNombreDesc().stream().map(TemporadaService::fila).toList();
    }

    @Transactional(readOnly = true)
    public TemporadaFila buscar(Long id) {
        return fila(temporadas.findById(id).orElseThrow(() -> new NoEncontradoException("Temporada")));
    }

    /**
     * Crea una temporada (sin activarla). Si se pide, copia las categorías de la activa
     * para ajustar después los años de nacimiento.
     */
    public Long crear(NuevaTemporadaForm form) {
        if (temporadas.existsByNombre(form.getNombre())) {
            throw new OperacionNoPermitidaException("Ya existe la temporada " + form.getNombre() + ".");
        }
        Temporada nueva = temporadas.save(new Temporada(form.getNombre()));
        if (form.isCopiarCategorias()) {
            temporadas.findFirstByActivaTrue().ifPresent(activa ->
                    categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(activa.getId())
                            .forEach(c -> categorias.save(c.copiarPara(nueva))));
        }
        return nueva.getId();
    }

    /** Activa una temporada y desactiva las demás. */
    public void activar(Long id) {
        Temporada elegida = temporadas.findById(id).orElseThrow(() -> new NoEncontradoException("Temporada"));
        temporadas.findAll().forEach(t -> t.setActiva(false));
        temporadas.flush();
        elegida.setActiva(true);
    }

    private static TemporadaFila fila(Temporada t) {
        return new TemporadaFila(t.getId(), t.getNombre(), t.isActiva());
    }
}
