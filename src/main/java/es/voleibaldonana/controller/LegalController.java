package es.voleibaldonana.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Páginas legales (textos en BORRADOR, pendientes de revisión por el club). */
@Controller
public class LegalController {

    @GetMapping("/aviso-legal")
    public String avisoLegal() {
        return "legal/aviso-legal";
    }

    @GetMapping("/privacidad")
    public String privacidad() {
        return "legal/privacidad";
    }

    @GetMapping("/cookies")
    public String cookies() {
        return "legal/cookies";
    }
}
