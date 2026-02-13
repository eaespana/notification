package org.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Representa el contenido de una notificación.
 * 
 * Esta clase encapsula toda la información relacionada con el contenido
 * que será enviado a través del canal de notificación. Soporta múltiples
 * formatos y adjuntos.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public final class NotificationContent {
    
    /**
     * Asunto o título de la notificación (útil para Email, Push, SMS truncado)
     */
    private final String subject;
    
    /**
     * Cuerpo de la notificación en texto plano
     */
    private final String body;
    
    /**
     * Cuerpo de la notificación en formato HTML (null si no aplica)
     */
    private final String html;
    
    /**
     * Lista de adjuntos a incluir en la notificación
     */
    private final List<Attachment> attachments;
    
    /**
     * Metadatos adicionales específicos del canal
     */
    private final Map<String, Object> metadata;
    
    /**
     * Plantilla a utilizar para renderizar el contenido (opcional)
     */
    private final String templateId;
    
    /**
     * Variables para sustituir en la plantilla
     */
    private final Map<String, Object> templateVariables;

    /**
     * Crea contenido simple con solo texto.
     *
     * @param body el cuerpo del mensaje
     * @return una nueva instancia de NotificationContent
     */
    public static NotificationContent plainText(String body) {
        return NotificationContent.builder()
            .body(body)
            .build();
    }
    
    /**
     * Crea contenido con asunto y cuerpo de texto.
     *
     * @param subject el asunto/título
     * @param body    el cuerpo del mensaje
     * @return una nueva instancia de NotificationContent
     */
    public static NotificationContent withSubject(String subject, String body) {
        return NotificationContent.builder()
            .subject(subject)
            .body(body)
            .build();
    }
    
    /**
     * Representa un archivo adjunto a incluir en la notificación.
     */
    @Getter
    @ToString
    @Builder
    public static final class Attachment {
        
        /**
         * Nombre del archivo
         */
        private final String filename;
        
        /**
         * Tipo MIME del contenido
         */
        private final String contentType;
        
        /**
         * Contenido del archivo en bytes
         */
        private final byte[] content;
        
        /**
         * URL remota del archivo (alternativa a contenido embebido)
         */
        private final String url;
        
        /**
         * Crea un attachment desde bytes.
         *
         * @param filename    el nombre del archivo
         * @param contentType el tipo MIME
         * @param content     el contenido en bytes
         * @return una nueva instancia de Attachment
         */
        public static Attachment fromBytes(String filename, String contentType, byte[] content) {
            return Attachment.builder()
                .filename(filename)
                .contentType(contentType)
                .content(content)
                .build();
        }
        
        /**
         * Crea un attachment desde una URL.
         *
         * @param filename    el nombre del archivo
         * @param contentType el tipo MIME
         * @param url         la URL del archivo
         * @return una nueva instancia de Attachment
         */
        public static Attachment fromUrl(String filename, String contentType, String url) {
            return Attachment.builder()
                .filename(filename)
                .contentType(contentType)
                .url(url)
                .build();
        }
    }
}
