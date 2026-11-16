package es.voleibaldonana.controller.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página de login del panel. El POST lo procesa Spring Security (ver SecurityConfig).
 * Si ya hay sesión iniciada, lleva directamente al panel.
 */
@Controller
public class LoginController {

    @GetMapping("/admin/login")
    public String login(Principal principal) {
        return principal != null ? "redirect:/admin" : "admin/login";
    }
}
