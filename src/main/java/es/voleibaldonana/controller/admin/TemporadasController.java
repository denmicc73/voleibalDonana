package es.voleibaldonana.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.voleibaldonana.dto.CategoriaForm;
import es.voleibaldonana.dto.NuevaTemporadaForm;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.service.CategoriaService;
import es.voleibaldonana.service.TemporadaService;
import jakarta.validation.Valid;

/** Temporadas y sus categorías (solo ADMIN, ver SecurityConfig). */
@Controller
@RequestMapping("/admin/temporadas")
public class TemporadasController {

    private static final String VISTA_LISTA = "admin/temporadas";
    private static final String VISTA_CATEGORIAS = "admin/categorias";
    private static final String VISTA_CATEGORIA_FORM = "admin/categoria-form";
    private static final String REDIRECT_LISTA = "redirect:/admin/temporadas";

    private final TemporadaService temporadaService;
    private final CategoriaService categoriaService;

    public TemporadasController(TemporadaService temporadaService, CategoriaService categoriaService) {
        this.temporadaService = temporadaService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("temporadas", temporadaService.listar());
        model.addAttribute("form", new NuevaTemporadaForm());
        return VISTA_LISTA;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") NuevaTemporadaForm form, BindingResult resultado, Model model,
            RedirectAttributes redirect) {
        if (!resultado.hasErrors()) {
            try {
                Long id = temporadaService.crear(form);
                redirect.addFlashAttribute("aviso", "Temporada " + form.getNombre()
                        + " creada. Revisa sus categorías y actívala cuando quieras que se vea en la web.");
                return "redirect:/admin/temporadas/" + id + "/categorias";
            } catch (OperacionNoPermitidaException e) {
                resultado.rejectValue("nombre", "duplicada", e.getMessage());
            }
        }
        model.addAttribute("temporadas", temporadaService.listar());
        return VISTA_LISTA;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirect) {
        temporadaService.activar(id);
        redirect.addFlashAttribute("aviso", "Temporada " + temporadaService.buscar(id).nombre()
                + " activada: es la que se muestra en la web.");
        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/categorias")
    public String categorias(@PathVariable Long id, Model model) {
        model.addAttribute("temporada", temporadaService.buscar(id));
        model.addAttribute("categorias", categoriaService.listar(id));
        return VISTA_CATEGORIAS;
    }

    @GetMapping("/{id}/categorias/nueva")
    public String nuevaCategoria(@PathVariable Long id, Model model) {
        model.addAttribute("temporada", temporadaService.buscar(id));
        model.addAttribute("form", new CategoriaForm());
        return VISTA_CATEGORIA_FORM;
    }

    @PostMapping("/{id}/categorias")
    public String crearCategoria(@PathVariable Long id, @Valid @ModelAttribute("form") CategoriaForm form,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (!resultado.hasErrors()) {
            try {
                categoriaService.crear(id, form);
                redirect.addFlashAttribute("aviso", "Categoría " + form.getNombre() + " creada.");
                return "redirect:/admin/temporadas/" + id + "/categorias";
            } catch (OperacionNoPermitidaException e) {
                resultado.rejectValue("nombre", "duplicada", e.getMessage());
            }
        }
        model.addAttribute("temporada", temporadaService.buscar(id));
        return VISTA_CATEGORIA_FORM;
    }

    @GetMapping("/categorias/{categoriaId}/editar")
    public String editarCategoria(@PathVariable Long categoriaId, Model model) {
        model.addAttribute("temporada", temporadaService.buscar(categoriaService.temporadaDe(categoriaId)));
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("form", categoriaService.formulario(categoriaId));
        return VISTA_CATEGORIA_FORM;
    }

    @PostMapping("/categorias/{categoriaId}")
    public String actualizarCategoria(@PathVariable Long categoriaId, @Valid @ModelAttribute("form") CategoriaForm form,
            BindingResult resultado, Model model, RedirectAttributes redirect) {
        Long temporadaId = categoriaService.temporadaDe(categoriaId);
        if (!resultado.hasErrors()) {
            try {
                categoriaService.actualizar(categoriaId, form);
                redirect.addFlashAttribute("aviso", "Categoría " + form.getNombre() + " guardada.");
                return "redirect:/admin/temporadas/" + temporadaId + "/categorias";
            } catch (OperacionNoPermitidaException e) {
                resultado.rejectValue("nombre", "duplicada", e.getMessage());
            }
        }
        model.addAttribute("temporada", temporadaService.buscar(temporadaId));
        model.addAttribute("categoriaId", categoriaId);
        return VISTA_CATEGORIA_FORM;
    }

    @PostMapping("/categorias/{categoriaId}/borrar")
    public String borrarCategoria(@PathVariable Long categoriaId, RedirectAttributes redirect) {
        Long temporadaId = categoriaService.temporadaDe(categoriaId);
        try {
            categoriaService.borrar(categoriaId);
            redirect.addFlashAttribute("aviso", "Categoría borrada.");
        } catch (OperacionNoPermitidaException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/temporadas/" + temporadaId + "/categorias";
    }
}
