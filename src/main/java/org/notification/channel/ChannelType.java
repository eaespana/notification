package org.notification.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeración que define los tipos de canales de notificación soportados.
 * 
 * Esta enumeración sirve como identificador único para cada canal y facilita
 * el registro y descubrimiento de canales en el sistema.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ChannelType {
    /** Canal de correo electrónico */
    EMAIL("email"),
    
    /** Canal de mensajes SMS */
    SMS("sms"),
    
    /** Canal de notificaciones push */
    PUSH("push"),
    
    /** Canal de mensajes de WhatsApp */
    WHATSAPP("whatsapp"),
    
    /** Canal de mensajes de Telegram */
    TELEGRAM("telegram"),
    
    /** Canal de notificaciones in-app */
    IN_APP("in_app");

    private final String identifier;

    /**
     * Obtiene el ChannelType a partir de su identificador textual.
     *
     * @param identifier el identificador del canal (case-insensitive)
     * @return el ChannelType correspondiente
     * @throws IllegalArgumentException si no existe un canal con ese identificador
     */
    public static ChannelType fromIdentifier(String identifier) {
        for (ChannelType type : values()) {
            if (type.identifier.equalsIgnoreCase(identifier)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown channel type: " + identifier);
    }
}
