package es.voleibaldonana.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.voleibaldonana.dto.SolicitudPatrocinioFila;
import es.voleibaldonana.dto.SolicitudPatrocinioForm;
import es.voleibaldonana.exception.NoEncontradoException;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.SolicitudPatrocinio;
import es.voleibaldonana.repository.SolicitudPatrocinioRepository;

/** Solicitudes del formulario "Patrocina al club" (bandeja solo ADMIN). */
@Service
@Transactional
public class SolicitudPatrocinioService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudPatrocinioService.class);

    private final SolicitudPatrocinioRepository solicitudes;
    private final LimiteEnviosService limite;
    private final Clock clock;

    public SolicitudPatrocinioService(SolicitudPatrocinioRepository solicitudes, LimiteEnviosService limite,
            Clock clock) {
        this.solicitudes = solicitudes;
        this.limite = limite;
        this.clock = clock;
    }

    /**
     * Guarda la solicitud. Si es de un bot (campo trampa relleno) no se guarda, pero no se avisa:
     * el bot ve lo mismo que una persona.
     *
     * @throws OperacionNoPermitidaException si desde esa IP se han enviado demasiadas
     */
    public void registrar(SolicitudPatrocinioForm form, String ip) {
        if (!limite.permitir("patrocinio:" + ip)) {
            throw new OperacionNoPermitidaException(
                    "Has enviado varias solicitudes seguidas. Espera un rato y vuelve a intentarlo.");
        }
        if (form.esBot()) {
            log.info("Solicitud de patrocinio descartada: campo trampa relleno");
            return;
        }
        solicitudes.save(new SolicitudPatrocinio(form.getEmpresa(), form.getContacto(), form.getEmail(),
                form.getTelefono(), form.getMensaje(), LocalDateTime.now(clock)));
    }

    @Transactional(readOnly = true)
    public List<SolicitudPatrocinioFila> listar() {
        return solicitudes.findAllByOrderByAtendidaAscRecibidaDesc().stream()
                .map(s -> new SolicitudPatrocinioFila(s.getId(), s.getEmpresa(), s.getContacto(), s.getEmail(),
                        s.getTelefono(), s.getMensaje(), s.getRecibida(), s.isAtendida()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long pendientes() {
        return solicitudes.countByAtendidaFalse();
    }

    public void marcarAtendida(Long id, boolean atendida) {
        buscar(id).setAtendida(atendida);
    }

    public void borrar(Long id) {
        solicitudes.delete(buscar(id));
    }

    private SolicitudPatrocinio buscar(Long id) {
        return solicitudes.findById(id).orElseThrow(() -> new NoEncontradoException("Solicitud"));
    }
}
