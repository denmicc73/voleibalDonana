package es.voleibaldonana.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.model.Usuario;
import es.voleibaldonana.repository.UsuarioRepository;

/**
 * Carga los usuarios del panel desde la base de datos para Spring Security.
 * Una cuenta desactivada o bloqueada por intentos fallidos no puede entrar.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;
    private final Clock clock;

    public UsuarioDetailsService(UsuarioRepository usuarios, Clock clock) {
        this.usuarios = usuarios;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarios.findByEmail(UsuarioService.normalizarEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .accountLocked(usuario.estaBloqueado(LocalDateTime.now(clock)))
                .build();
    }
}
