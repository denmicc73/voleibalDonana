package es.voleibaldonana.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import es.voleibaldonana.dto.SolicitudPatrocinioForm;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.service.PatrocinadorService;
import es.voleibaldonana.service.SolicitudPatrocinioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** Parte pública del patrocinio: página "Patrocina al club" y logos de los patrocinadores. */
@Controller
public class PatrocinioController {

    private static final String VISTA = "patrocina";

    private final SolicitudPatrocinioService solicitudService;
    private final PatrocinadorService patrocinadorService;

    public PatrocinioController(SolicitudPatrocinioService solicitudService, PatrocinadorService patrocinadorService) {
        this.solicitudService = solicitudService;
        this.patrocinadorService = patrocinadorService;
    }

    @GetMapping("/patrocina")
    public String formulario(Model model) {
        model.addAttribute("form", new SolicitudPatrocinioForm());
        model.addAttribute("patrocinadores", patrocinadorService.visibles());
        return VISTA;
    }

    @PostMapping("/patrocina")
    public String enviar(@Valid @ModelAttribute("form") SolicitudPatrocinioForm form, BindingResult resultado,
            HttpServletRequest peticion, Model model) {
        if (!resultado.hasErrors()) {
            try {
                solicitudService.registrar(form, peticion.getRemoteAddr());
                return "redirect:/patrocina/gracias";
            } catch (OperacionNoPermitidaException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("patrocinadores", patrocinadorService.visibles());
        return VISTA;
    }

    @GetMapping("/patrocina/gracias")
    public String gracias() {
        return "patrocina-gracias";
    }

    /** Logo de un patrocinador. La URL lleva la versión (?v=), así que se puede guardar en caché. */
    @GetMapping("/patrocinadores/{id}/logo")
    public ResponseEntity<byte[]> logo(@PathVariable Long id) {
        return patrocinadorService.logo(id)
                .map(imagen -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(imagen.tipo()))
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                        .body(imagen.datos()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
