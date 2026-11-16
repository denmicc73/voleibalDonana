package es.voleibaldonana.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.voleibaldonana.service.SolicitudPatrocinioService;

/** Bandeja de solicitudes de patrocinio (solo ADMIN: datos de contacto). */
@Controller
@RequestMapping("/admin/solicitudes-patrocinio")
public class SolicitudesPatrocinioController {

    private static final String REDIRECT_LISTA = "redirect:/admin/solicitudes-patrocinio";

    private final SolicitudPatrocinioService solicitudService;

    public SolicitudesPatrocinioController(SolicitudPatrocinioService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("solicitudes", solicitudService.listar());
        return "admin/solicitudes-patrocinio";
    }

    @PostMapping("/{id}/atendida")
    public String marcarAtendida(@PathVariable Long id, RedirectAttributes redirect) {
        solicitudService.marcarAtendida(id, true);
        redirect.addFlashAttribute("aviso", "Solicitud marcada como atendida.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/pendiente")
    public String marcarPendiente(@PathVariable Long id, RedirectAttributes redirect) {
        solicitudService.marcarAtendida(id, false);
        redirect.addFlashAttribute("aviso", "Solicitud marcada como pendiente.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes redirect) {
        solicitudService.borrar(id);
        redirect.addFlashAttribute("aviso", "Solicitud borrada.");
        return REDIRECT_LISTA;
    }
}
