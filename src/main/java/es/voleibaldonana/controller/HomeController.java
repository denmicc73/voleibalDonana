package es.voleibaldonana.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.voleibaldonana.service.PatrocinadorService;
import es.voleibaldonana.service.PortadaService;

/** Portada: marcador, equipos y calendario de la temporada activa, y patrocinadores. */
@Controller
public class HomeController {

    private final PortadaService portadaService;
    private final PatrocinadorService patrocinadorService;

    public HomeController(PortadaService portadaService, PatrocinadorService patrocinadorService) {
        this.portadaService = portadaService;
        this.patrocinadorService = patrocinadorService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("portada", portadaService.datos());
        model.addAttribute("patrocinadores", patrocinadorService.visibles());
        return "index";
    }
}
