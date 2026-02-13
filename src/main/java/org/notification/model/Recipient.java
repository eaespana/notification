package org.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Representa un destinatario de una notificación.
 * 
 * Esta clase encapsula la información de contacto de un destinatario,
 * incluyendo su identificador único, tipo de destinatario y metadatos adicionales.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public class Recipient {
    
    /**
     * Identificador único del destinatario (email, número de teléfono, user ID, etc.)
     */
    private final String identifier;
    
    /**
     * Nombre display del destinatario (opcional)
     */
    private final String displayName;
    
    /**
     * Tipo de destinatario según la clasificación de correo electrónico
     */
    @Builder.Default
    private final RecipientType type = RecipientType.TO;
    
    /**
     * Metadatos adicionales específicos del canal
     * (ej. deviceToken para push, chatId para Telegram)
     */
    private final java.util.Map<String, Object> metadata;

    /**
     * Crea un destinatario con el identificador mínimo requerido.
     *
     * @param identifier el identificador del destinatario
     * @return una nueva instancia de Recipient
     */
    public static Recipient of(String identifier) {
        return Recipient.builder()
            .identifier(identifier)
            .build();
    }
    
    /**
     * Crea un destinatario de tipo TO (principal).
     *
     * @param identifier el identificador del destinatario
     * @return una nueva instancia de Recipient
     */
    public static Recipient to(String identifier) {
        return Recipient.builder()
            .identifier(identifier)
            .type(RecipientType.TO)
            .build();
    }
    
    /**
     * Crea un destinatario en copia (CC).
     *
     * @param identifier el identificador del destinatario
     * @return una nueva instancia de Recipient
     */
    public static Recipient cc(String identifier) {
        return Recipient.builder()
            .identifier(identifier)
            .type(RecipientType.CC)
            .build();
    }
    
    /**
     * Crea un destinatario en copia oculta (CCO/BCC).
     *
     * @param identifier el identificador del destinatario
     * @return una nueva instancia de Recipient
     */
    public static Recipient bcc(String identifier) {
        return Recipient.builder()
            .identifier(identifier)
            .type(RecipientType.BCC)
            .build();
    }
}
