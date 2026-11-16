package es.voleibaldonana.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Límite sencillo de envíos de formularios públicos por IP (en memoria): como máximo
 * {@link #MAX_ENVIOS} cada {@link #VENTANA}. Frena bots y envíos repetidos; con varias
 * instancias del servidor cada una lleva su cuenta, suficiente para la web de un club.
 */
@Service
public class LimiteEnviosService {

    static final int MAX_ENVIOS = 5;
    static final Duration VENTANA = Duration.ofHours(1);

    /** Tope de IPs recordadas, para que la memoria no crezca sin límite. */
    private static final int MAX_CLAVES = 10_000;

    private final Map<String, Deque<Instant>> envios = new ConcurrentHashMap<>();
    private final Clock clock;

    public LimiteEnviosService(Clock clock) {
        this.clock = clock;
    }

    /** Registra un envío de esa clave (formulario + IP) y dice si está dentro del límite. */
    public boolean permitir(String clave) {
        Instant ahora = clock.instant();
        Instant limite = ahora.minus(VENTANA);
        if (envios.size() > MAX_CLAVES) {
            envios.values().removeIf(cola -> cola.isEmpty() || cola.peekLast().isBefore(limite));
        }
        Deque<Instant> cola = envios.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
                cola.pollFirst();
            }
            if (cola.size() >= MAX_ENVIOS) {
                return false;
            }
            cola.addLast(ahora);
            return true;
        }
    }

    /** Olvida todos los envíos (para los tests). */
    public void reiniciar() {
        envios.clear();
    }
}
