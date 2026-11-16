package es.voleibaldonana.controller.admin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.voleibaldonana.service.SolicitudPatrocinioService;

/** Portada del panel de administración. */
@Controller
public class PanelController {

    private final SolicitudPatrocinioService solicitudService;

    public PanelController(SolicitudPatrocinioService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping("/admin")
    public String inicio(Authentication autenticacion, Model model) {
        boolean admin = autenticacion.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("solicitudesPendientes", admin ? solicitudService.pendientes() : 0L);
        return "admin/inicio";
    }
}
