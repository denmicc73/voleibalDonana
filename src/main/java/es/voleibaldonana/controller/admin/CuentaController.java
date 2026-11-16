package es.voleibaldonana.controller.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.voleibaldonana.dto.CambioPasswordForm;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.exception.PasswordIncorrectaException;
import es.voleibaldonana.service.UsuarioService;
import jakarta.validation.Valid;

/** "Mi cuenta": cada persona puede cambiar su propia contraseña. */
@Controller
@RequestMapping("/admin/cuenta")
public class CuentaController {

    private static final String VISTA = "admin/cuenta";

    private final UsuarioService usuarioService;

    public CuentaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("form", new CambioPasswordForm());
        return VISTA;
    }

    @PostMapping
    public String cambiarPassword(@Valid @ModelAttribute("form") CambioPasswordForm form, BindingResult resultado,
            Principal principal, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return VISTA;
        }
        try {
            usuarioService.cambiarPassword(principal.getName(), form.getActual(), form.getNueva());
        } catch (PasswordIncorrectaException e) {
            resultado.rejectValue("actual", "incorrecta", e.getMessage());
            return VISTA;
        } catch (OperacionNoPermitidaException e) {
            resultado.rejectValue("nueva", "invalida", e.getMessage());
            return VISTA;
        }
        redirect.addFlashAttribute("aviso", "Contraseña cambiada correctamente.");
        return "redirect:/admin/cuenta";
    }
}
