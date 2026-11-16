package es.voleibaldonana.controller.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.voleibaldonana.dto.NuevoUsuarioForm;
import es.voleibaldonana.dto.RestablecerPasswordForm;
import es.voleibaldonana.exception.EmailEnUsoException;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.Rol;
import es.voleibaldonana.service.UsuarioService;
import jakarta.validation.Valid;

/** Gestión de las personas con acceso al panel. Solo ADMIN (ver SecurityConfig). */
@Controller
@RequestMapping("/admin/usuarios")
public class UsuariosController {

    private static final String VISTA_LISTA = "admin/usuarios";
    private static final String VISTA_NUEVO = "admin/usuario-nuevo";
    private static final String VISTA_PASSWORD = "admin/usuario-password";
    private static final String REDIRECT_LISTA = "redirect:/admin/usuarios";

    private final UsuarioService usuarioService;

    public UsuariosController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("roles")
    public Rol[] roles() {
        return Rol.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("form", new NuevoUsuarioForm());
        return VISTA_NUEVO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") NuevoUsuarioForm form, BindingResult resultado,
            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return VISTA_NUEVO;
        }
        try {
            usuarioService.crear(form);
        } catch (EmailEnUsoException e) {
            resultado.rejectValue("email", "enUso", e.getMessage());
            return VISTA_NUEVO;
        } catch (OperacionNoPermitidaException e) {
            resultado.rejectValue("password", "invalida", e.getMessage());
            return VISTA_NUEVO;
        }
        redirect.addFlashAttribute("aviso", "Usuario creado. Comunícale su contraseña en persona y pídele que la cambie.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        usuarioService.cambiarActivo(id, true, principal.getName());
        redirect.addFlashAttribute("aviso", "Cuenta activada.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            usuarioService.cambiarActivo(id, false, principal.getName());
            redirect.addFlashAttribute("aviso", "Cuenta desactivada.");
        } catch (OperacionNoPermitidaException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/password")
    public String formularioPassword(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.resumen(id));
        model.addAttribute("form", new RestablecerPasswordForm());
        return VISTA_PASSWORD;
    }

    @PostMapping("/{id}/password")
    public String restablecerPassword(@PathVariable Long id,
            @Valid @ModelAttribute("form") RestablecerPasswordForm form, BindingResult resultado,
            Principal principal, Model model, RedirectAttributes redirect) {
        if (!resultado.hasErrors()) {
            try {
                usuarioService.restablecerPassword(id, form.getPassword(), principal.getName());
                redirect.addFlashAttribute("aviso",
                        "Contraseña restablecida. Comunícala en persona y pide que la cambie en \"Mi cuenta\".");
                return REDIRECT_LISTA;
            } catch (OperacionNoPermitidaException e) {
                resultado.rejectValue("password", "invalida", e.getMessage());
            }
        }
        model.addAttribute("usuario", usuarioService.resumen(id));
        return VISTA_PASSWORD;
    }

    @PostMapping("/{id}/desbloquear")
    public String desbloquear(@PathVariable Long id, RedirectAttributes redirect) {
        usuarioService.desbloquear(id);
        redirect.addFlashAttribute("aviso", "Cuenta desbloqueada.");
        return REDIRECT_LISTA;
    }
}
