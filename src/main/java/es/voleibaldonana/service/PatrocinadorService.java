package es.voleibaldonana.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.Imagen;
import es.voleibaldonana.dto.PatrocinadorFila;
import es.voleibaldonana.dto.PatrocinadorForm;
import es.voleibaldonana.exception.NoEncontradoException;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.LogoPatrocinador;
import es.voleibaldonana.model.Patrocinador;
import es.voleibaldonana.repository.LogoPatrocinadorRepository;
import es.voleibaldonana.repository.PatrocinadorRepository;

/** Patrocinadores y sus logos (gestión solo ADMIN; los visibles salen en la portada). */
@Service
@Transactional
public class PatrocinadorService {

    private final PatrocinadorRepository patrocinadores;
    private final LogoPatrocinadorRepository logos;
    private final Clock clock;

    public PatrocinadorService(PatrocinadorRepository patrocinadores, LogoPatrocinadorRepository logos, Clock clock) {
        this.patrocinadores = patrocinadores;
        this.logos = logos;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PatrocinadorFila> listar() {
        return patrocinadores.findAllByOrderByNivelDescOrdenAscNombreAsc().stream()
                .map(PatrocinadorService::fila).toList();
    }

    @Transactional(readOnly = true)
    public List<PatrocinadorFila> visibles() {
        return patrocinadores.findByVisibleTrueOrderByNivelDescOrdenAscNombreAsc().stream()
                .map(PatrocinadorService::fila).toList();
    }

    @Transactional(readOnly = true)
    public PatrocinadorFila vista(Long id) {
        return fila(buscar(id));
    }

    @Transactional(readOnly = true)
    public PatrocinadorForm formulario(Long id) {
        Patrocinador p = buscar(id);
        PatrocinadorForm form = new PatrocinadorForm();
        form.setNombre(p.getNombre());
        form.setWeb(p.getWeb());
        form.setNivel(p.getNivel());
        form.setOrden(p.getOrden());
        form.setVisible(p.isVisible());
        return form;
    }

    /** @param logo bytes del archivo subido, o nulo/vacío si no se sube ninguno */
    public Long crear(PatrocinadorForm form, byte[] logo) {
        String tipo = validarLogo(logo);
        Patrocinador p = new Patrocinador();
        copiar(form, p);
        patrocinadores.save(p);
        if (tipo != null) {
            guardarLogo(p, logo, tipo);
        }
        return p.getId();
    }

    public void actualizar(Long id, PatrocinadorForm form, byte[] logo) {
        String tipo = validarLogo(logo);
        Patrocinador p = buscar(id);
        copiar(form, p);
        if (tipo != null) {
            guardarLogo(p, logo, tipo);
        } else if (form.isQuitarLogo() && p.tieneLogo()) {
            logos.deleteById(id);
            p.quitarLogo();
        }
    }

    public void borrar(Long id) {
        Patrocinador p = buscar(id);
        logos.findById(id).ifPresent(logos::delete);
        patrocinadores.delete(p);
    }

    @Transactional(readOnly = true)
    public Optional<Imagen> logo(Long id) {
        return patrocinadores.findById(id)
                .filter(Patrocinador::tieneLogo)
                .flatMap(p -> logos.findById(id).map(l -> new Imagen(l.getDatos(), p.getLogoTipo())));
    }

    /** Devuelve el tipo MIME si hay un logo válido, nulo si no se ha subido ninguno. */
    static String validarLogo(byte[] logo) {
        if (logo == null || logo.length == 0) {
            return null;
        }
        if (logo.length > TipoImagen.MAX_BYTES) {
            throw new OperacionNoPermitidaException("El logo ocupa demasiado: como máximo 1 MB.");
        }
        return TipoImagen.detectar(logo).orElseThrow(() ->
                new OperacionNoPermitidaException("El logo tiene que ser una imagen PNG, JPG o WebP."));
    }

    private void guardarLogo(Patrocinador p, byte[] datos, String tipo) {
        LogoPatrocinador logo = logos.findById(p.getId()).orElseGet(() -> new LogoPatrocinador(p.getId(), datos));
        logo.setDatos(datos);
        logos.save(logo);
        p.registrarLogo(tipo, clock.millis());
    }

    private Patrocinador buscar(Long id) {
        return patrocinadores.findById(id).orElseThrow(() -> new NoEncontradoException("Patrocinador"));
    }

    private static void copiar(PatrocinadorForm form, Patrocinador p) {
        p.setNombre(form.getNombre());
        p.setWeb(form.getWeb());
        p.setNivel(form.getNivel());
        p.setOrden(form.getOrden());
        p.setVisible(form.isVisible());
    }

    private static PatrocinadorFila fila(Patrocinador p) {
        String logoUrl = p.tieneLogo() ? "/patrocinadores/" + p.getId() + "/logo?v=" + p.getLogoVersion() : null;
        return new PatrocinadorFila(p.getId(), p.getNombre(), p.getWeb(), p.getNivel(), p.getOrden(), p.isVisible(),
                logoUrl);
    }
}
