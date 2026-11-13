package es.voleibaldonana.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.voleibaldonana.service.DatosEjemploService;

/** Portada. De momento con datos de ejemplo (ver {@link DatosEjemploService}). */
@Controller
public class HomeController {

    private final DatosEjemploService datosEjemplo;

    public HomeController(DatosEjemploService datosEjemplo) {
        this.datosEjemplo = datosEjemplo;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("ultimoResultado", datosEjemplo.ultimoResultado());
        model.addAttribute("proximoPartido", datosEjemplo.proximoPartido());
        model.addAttribute("categorias", datosEjemplo.categorias());
        model.addAttribute("temporada", datosEjemplo.temporada());
        model.addAttribute("calendario", datosEjemplo.calendario());
        model.addAttribute("aniosNacimiento", datosEjemplo.aniosNacimiento());
        return "index";
    }
}
