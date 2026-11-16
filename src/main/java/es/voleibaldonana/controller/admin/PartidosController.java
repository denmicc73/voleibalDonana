package es.voleibaldonana.controller.admin;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.voleibaldonana.dto.PartidoForm;
import es.voleibaldonana.dto.ResultadoForm;
import es.voleibaldonana.dto.TemporadaFila;
import es.voleibaldonana.exception.ResultadoNoValidoException;
import es.voleibaldonana.model.EstadoPartido;
import es.voleibaldonana.model.Genero;
import es.voleibaldonana.service.CategoriaService;
import es.voleibaldonana.service.PartidoService;
import es.voleibaldonana.service.TemporadaService;
import jakarta.validation.Valid;

/** Partidos y resultados de la temporada activa (ADMIN y EDITOR). */
@Controller
@RequestMapping("/admin/partidos")
public class PartidosController {

    private static final String VISTA_LISTA = "admin/partidos";
    private static final String VISTA_FORM = "admin/partido-form";
    private static final String VISTA_RESULTADO = "admin/partido-resultado";
    private static final String VISTA_BORRAR = "admin/partido-borrar";
    private static final String REDIRECT_LISTA = "redirect:/admin/partidos";

    private final PartidoService partidoService;
    private final TemporadaService temporadaService;
    private final CategoriaService categoriaService;

    public PartidosController(PartidoService partidoService, TemporadaService temporadaService,
            CategoriaService categoriaService) {
        this.partidoService = partidoService;
        this.temporadaService = temporadaService;
        this.categoriaService = categoriaService;
    }

    @ModelAttribute("generos")
    public Genero[] generos() {
        return Genero.values();
    }

    /** Estados que se eligen a mano (FINALIZADO llega al guardar el resultado). */
    @ModelAttribute("estados")
    public List<EstadoPartido> estados() {
        return Arrays.stream(EstadoPartido.values()).filter(e -> e != EstadoPartido.FINALIZADO).toList();
    }

    @GetMapping
    public String listar(@RequestParam(name = "categoria", required = false) Long categoriaId, Model model) {
        TemporadaFila temporada = temporadaService.activa().orElse(null);
        model.addAttribute("temporada", temporada);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        if (temporada != null) {
            model.addAttribute("categorias", categoriaService.listar(temporada.id()));
            model.addAttribute("partidos", partidoService.listar(temporada.id(), categoriaId));
        }
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model, RedirectAttributes redirect) {
        TemporadaFila temporada = temporadaService.activa().orElse(null);
        if (temporada == null) {
            redirect.addFlashAttribute("error", "No hay temporada activa: créala o actívala en Temporadas.");
            return REDIRECT_LISTA;
        }
        model.addAttribute("form", new PartidoForm());
        model.addAttribute("categorias", categoriaService.listar(temporada.id()));
        return VISTA_FORM;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") PartidoForm form, BindingResult resultado, Model model,
            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            temporadaService.activa().ifPresent(t -> model.addAttribute("categorias", categoriaService.listar(t.id())));
            return VISTA_FORM;
        }
        Long id = partidoService.crear(form);
        redirect.addFlashAttribute("aviso", "Partido creado.");
        return "redirect:/admin/partidos?categoria=" + form.getCategoriaId() + "#partido-" + id;
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        PartidoForm form = partidoService.formulario(id);
        model.addAttribute("form", form);
        model.addAttribute("partido", partidoService.vista(id));
        model.addAttribute("categorias", categoriaService.listar(categoriaService.temporadaDe(form.getCategoriaId())));
        return VISTA_FORM;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") PartidoForm form,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("partido", partidoService.vista(id));
            model.addAttribute("categorias",
                    categoriaService.listar(categoriaService.temporadaDe(partidoService.formulario(id).getCategoriaId())));
            return VISTA_FORM;
        }
        partidoService.actualizar(id, form);
        redirect.addFlashAttribute("aviso", "Partido guardado.");
        return "redirect:/admin/partidos?categoria=" + form.getCategoriaId() + "#partido-" + id;
    }

    @GetMapping("/{id}/resultado")
    public String formularioResultado(@PathVariable Long id, Model model) {
        model.addAttribute("partido", partidoService.vista(id));
        model.addAttribute("form", partidoService.resultadoFormulario(id));
        return VISTA_RESULTADO;
    }

    @PostMapping("/{id}/resultado")
    public String guardarResultado(@PathVariable Long id, @ModelAttribute("form") ResultadoForm form,
            BindingResult binding, Model model, RedirectAttributes redirect) {
        List<String> errores;
        if (binding.hasErrors()) {
            errores = List.of("Los puntos tienen que ser números enteros.");
        } else {
            try {
                partidoService.guardarResultado(id, form);
                errores = List.of();
            } catch (ResultadoNoValidoException e) {
                errores = e.getErrores();
            }
        }
        if (!errores.isEmpty()) {
            model.addAttribute("partido", partidoService.vista(id));
            model.addAttribute("erroresResultado", errores);
            return VISTA_RESULTADO;
        }
        redirect.addFlashAttribute("aviso", "Resultado guardado.");
        return "redirect:/admin/partidos?categoria=" + partidoService.vista(id).categoriaId() + "#partido-" + id;
    }

    @PostMapping("/{id}/resultado/quitar")
    public String quitarResultado(@PathVariable Long id, RedirectAttributes redirect) {
        partidoService.quitarResultado(id);
        redirect.addFlashAttribute("aviso", "Resultado quitado: el partido vuelve a estar programado.");
        return "redirect:/admin/partidos/" + id + "/resultado";
    }

    @GetMapping("/{id}/borrar")
    public String confirmarBorrado(@PathVariable Long id, Model model) {
        model.addAttribute("partido", partidoService.vista(id));
        return VISTA_BORRAR;
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes redirect) {
        partidoService.borrar(id);
        redirect.addFlashAttribute("aviso", "Partido borrado.");
        return REDIRECT_LISTA;
    }
}
