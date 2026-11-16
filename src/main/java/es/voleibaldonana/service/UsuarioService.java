package es.voleibaldonana.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.NuevoUsuarioForm;
import es.voleibaldonana.dto.PoliticaPassword;
import es.voleibaldonana.dto.UsuarioActual;
import es.voleibaldonana.dto.UsuarioResumen;
import es.voleibaldonana.exception.EmailEnUsoException;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.exception.PasswordIncorrectaException;
import es.voleibaldonana.exception.UsuarioNoEncontradoException;
import es.voleibaldonana.model.Rol;
import es.voleibaldonana.model.Usuario;
import es.voleibaldonana.repository.UsuarioRepository;

/** Gestión de las personas con acceso al panel. */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SesionesService sesiones;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, Clock clock,
            SesionesService sesiones) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.sesiones = sesiones;
    }

    /** El email es el nombre de usuario: se guarda y se busca siempre en minúsculas y sin espacios. */
    public static String normalizarEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public boolean hayAdministrador() {
        return usuarios.existsByRol(Rol.ADMIN);
    }

    public Long crear(NuevoUsuarioForm form) {
        return crear(form.getNombre(), form.getEmail(), form.getPassword(), form.getRol());
    }

    public Long crear(String nombre, String email, String password, Rol rol) {
        String emailNormalizado = normalizarEmail(email);
        if (!PoliticaPassword.valida(password)) {
            throw new OperacionNoPermitidaException(PoliticaPassword.MENSAJE);
        }
        if (usuarios.existsByEmail(emailNormalizado)) {
            throw new EmailEnUsoException();
        }
        Usuario usuario = new Usuario(nombre.strip(), emailNormalizado, passwordEncoder.encode(password), rol,
                LocalDateTime.now(clock));
        return usuarios.save(usuario).getId();
    }

    public void cambiarPassword(String email, String actual, String nueva) {
        Usuario usuario = buscarPorEmail(email);
        if (!passwordEncoder.matches(actual, usuario.getPasswordHash())) {
            throw new PasswordIncorrectaException();
        }
        if (!PoliticaPassword.valida(nueva)) {
            throw new OperacionNoPermitidaException(PoliticaPassword.MENSAJE);
        }
        usuario.setPasswordHash(passwordEncoder.encode(nueva));
    }

    /**
     * Activa o desactiva una cuenta. Nadie puede desactivarse a sí mismo: así siempre queda
     * al menos un administrador activo (quien está usando el panel).
     * Al activar, se quita también un posible bloqueo por intentos fallidos.
     * Al desactivar, se cierran sus sesiones abiertas.
     */
    public void cambiarActivo(Long id, boolean activo, String emailQuienLoHace) {
        Usuario usuario = usuarios.findById(id).orElseThrow(UsuarioNoEncontradoException::new);
        if (!activo && usuario.getEmail().equals(normalizarEmail(emailQuienLoHace))) {
            throw new OperacionNoPermitidaException("No puedes desactivar tu propia cuenta.");
        }
        usuario.setActivo(activo);
        if (activo) {
            usuario.desbloquear();
        } else {
            sesiones.cerrarSesiones(usuario.getEmail());
        }
    }

    public void desbloquear(Long id) {
        usuarios.findById(id).orElseThrow(UsuarioNoEncontradoException::new).desbloquear();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumen> listar() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        return usuarios.findAllByOrderByNombreAsc().stream()
                .map(u -> new UsuarioResumen(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo(),
                        u.estaBloqueado(ahora), u.getUltimoAcceso()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioActual> usuarioActual(String email) {
        return usuarios.findByEmail(normalizarEmail(email))
                .map(u -> new UsuarioActual(u.getNombre(), u.getEmail(), u.getRol() == Rol.ADMIN));
    }

    private Usuario buscarPorEmail(String email) {
        return usuarios.findByEmail(normalizarEmail(email)).orElseThrow(UsuarioNoEncontradoException::new);
    }
}
