package es.voleibaldonana.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.voleibaldonana.service.PortadaService;

/** Portada: marcador, equipos y calendario de la temporada activa. */
@Controller
public class HomeController {

    private final PortadaService portadaService;

    public HomeController(PortadaService portadaService) {
        this.portadaService = portadaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("portada", portadaService.datos());
        return "index";
    }
}
