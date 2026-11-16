package es.voleibaldonana.controller.admin;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.voleibaldonana.dto.PatrocinadorForm;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.NivelPatrocinio;
import es.voleibaldonana.service.PatrocinadorService;
import jakarta.validation.Valid;

/** Patrocinadores que salen en la web (solo ADMIN). */
@Controller
@RequestMapping("/admin/patrocinadores")
public class PatrocinadoresController {

    private static final String VISTA_LISTA = "admin/patrocinadores";
    private static final String VISTA_FORM = "admin/patrocinador-form";
    private static final String REDIRECT_LISTA = "redirect:/admin/patrocinadores";

    private final PatrocinadorService patrocinadorService;

    public PatrocinadoresController(PatrocinadorService patrocinadorService) {
        this.patrocinadorService = patrocinadorService;
    }

    @ModelAttribute("niveles")
    public NivelPatrocinio[] niveles() {
        return NivelPatrocinio.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("patrocinadores", patrocinadorService.listar());
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("form", new PatrocinadorForm());
        return VISTA_FORM;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") PatrocinadorForm form, BindingResult resultado,
            @RequestParam(name = "logo", required = false) MultipartFile logo, RedirectAttributes redirect)
            throws IOException {
        if (!resultado.hasErrors()) {
            try {
                patrocinadorService.crear(form, bytes(logo));
                redirect.addFlashAttribute("aviso", "Patrocinador " + form.getNombre() + " añadido.");
                return REDIRECT_LISTA;
            } catch (OperacionNoPermitidaException e) {
                resultado.reject("logo", e.getMessage());
            }
        }
        return VISTA_FORM;
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("patrocinador", patrocinadorService.vista(id));
        model.addAttribute("form", patrocinadorService.formulario(id));
        return VISTA_FORM;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") PatrocinadorForm form,
            BindingResult resultado, @RequestParam(name = "logo", required = false) MultipartFile logo, Model model,
            RedirectAttributes redirect) throws IOException {
        if (!resultado.hasErrors()) {
            try {
                patrocinadorService.actualizar(id, form, bytes(logo));
                redirect.addFlashAttribute("aviso", "Patrocinador " + form.getNombre() + " guardado.");
                return REDIRECT_LISTA;
            } catch (OperacionNoPermitidaException e) {
                resultado.reject("logo", e.getMessage());
            }
        }
        model.addAttribute("patrocinador", patrocinadorService.vista(id));
        return VISTA_FORM;
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes redirect) {
        patrocinadorService.borrar(id);
        redirect.addFlashAttribute("aviso", "Patrocinador borrado.");
        return REDIRECT_LISTA;
    }

    private static byte[] bytes(MultipartFile archivo) throws IOException {
        return archivo == null || archivo.isEmpty() ? null : archivo.getBytes();
    }
}
