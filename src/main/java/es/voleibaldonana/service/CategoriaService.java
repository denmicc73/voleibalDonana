package es.voleibaldonana.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.CategoriaFila;
import es.voleibaldonana.dto.CategoriaForm;
import es.voleibaldonana.exception.NoEncontradoException;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.Categoria;
import es.voleibaldonana.model.Temporada;
import es.voleibaldonana.repository.CategoriaRepository;
import es.voleibaldonana.repository.PartidoRepository;
import es.voleibaldonana.repository.TemporadaRepository;

/** Categorías de cada temporada (años de nacimiento, horario, orden y si se muestran en la web). */
@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final TemporadaRepository temporadas;
    private final PartidoRepository partidos;

    public CategoriaService(CategoriaRepository categorias, TemporadaRepository temporadas,
            PartidoRepository partidos) {
        this.categorias = categorias;
        this.temporadas = temporadas;
        this.partidos = partidos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaFila> listar(Long temporadaId) {
        return categorias.findByTemporadaIdOrderByOrdenAscNombreAsc(temporadaId).stream()
                .map(Vistas::categoria).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaForm formulario(Long id) {
        Categoria c = buscar(id);
        CategoriaForm form = new CategoriaForm();
        form.setNombre(c.getNombre());
        form.setDescripcionEdad(c.getDescripcionEdad());
        form.setAnioDesde(c.getAnioDesde());
        form.setAnioHasta(c.getAnioHasta());
        form.setHorario(c.getHorario());
        form.setOrden(c.getOrden());
        form.setVisible(c.isVisible());
        return form;
    }

    @Transactional(readOnly = true)
    public Long temporadaDe(Long categoriaId) {
        return buscar(categoriaId).getTemporada().getId();
    }

    public Long crear(Long temporadaId, CategoriaForm form) {
        Temporada temporada = temporadas.findById(temporadaId)
                .orElseThrow(() -> new NoEncontradoException("Temporada"));
        if (categorias.existsByTemporadaIdAndNombreIgnoreCase(temporadaId, form.getNombre())) {
            throw new OperacionNoPermitidaException("Ya hay una categoría con ese nombre en esta temporada.");
        }
        Categoria nueva = new Categoria(temporada);
        copiar(form, nueva);
        return categorias.save(nueva).getId();
    }

    public void actualizar(Long id, CategoriaForm form) {
        Categoria c = buscar(id);
        if (categorias.existsByTemporadaIdAndNombreIgnoreCaseAndIdNot(c.getTemporada().getId(), form.getNombre(), id)) {
            throw new OperacionNoPermitidaException("Ya hay una categoría con ese nombre en esta temporada.");
        }
        copiar(form, c);
    }

    /** Solo se borra si no tiene partidos; si los tiene, se oculta (visible = no). */
    public void borrar(Long id) {
        Categoria c = buscar(id);
        if (partidos.existsByCategoriaId(id)) {
            throw new OperacionNoPermitidaException(
                    "La categoría " + c.getNombre() + " tiene partidos: no se puede borrar. Puedes ocultarla.");
        }
        categorias.delete(c);
    }

    private Categoria buscar(Long id) {
        return categorias.findById(id).orElseThrow(() -> new NoEncontradoException("Categoría"));
    }

    private static void copiar(CategoriaForm form, Categoria c) {
        c.setNombre(form.getNombre());
        c.setDescripcionEdad(form.getDescripcionEdad());
        c.setAnioDesde(form.getAnioDesde());
        c.setAnioHasta(form.getAnioHasta());
        c.setHorario(form.getHorario());
        c.setOrden(form.getOrden());
        c.setVisible(form.isVisible());
    }
}
