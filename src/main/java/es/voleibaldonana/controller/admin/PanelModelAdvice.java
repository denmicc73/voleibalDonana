package es.voleibaldonana.controller.admin;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.voleibaldonana.dto.UsuarioActual;
import es.voleibaldonana.service.UsuarioService;

/** Añade a todas las vistas del panel los datos de la persona conectada (para la cabecera). */
@ControllerAdvice(basePackageClasses = PanelModelAdvice.class)
public class PanelModelAdvice {

    private final UsuarioService usuarioService;

    public PanelModelAdvice(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("usuarioActual")
    public UsuarioActual usuarioActual(Principal principal) {
        return principal == null ? null : usuarioService.usuarioActual(principal.getName()).orElse(null);
    }
}
