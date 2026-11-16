package es.voleibaldonana.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Portada del panel de administración. */
@Controller
public class PanelController {

    @GetMapping("/admin")
    public String inicio() {
        return "admin/inicio";
    }
}
