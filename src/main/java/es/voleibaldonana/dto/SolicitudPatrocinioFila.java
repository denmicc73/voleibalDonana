package es.voleibaldonana.dto;

import java.time.LocalDateTime;

/** Solicitud de patrocinio en la bandeja del panel. */
public record SolicitudPatrocinioFila(
        Long id,
        String empresa,
        String contacto,
        String email,
        String telefono,
        String mensaje,
        LocalDateTime recibida,
        boolean atendida) {
}
