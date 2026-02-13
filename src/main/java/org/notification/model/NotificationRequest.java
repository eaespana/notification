package org.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.notification.channel.ChannelType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representa una solicitud completa de envío de notificación.
 * 
 * Esta clase es el objeto principal que viaja a través del sistema.
 * Contiene todo lo necesario para que un canal procese y envíe
 * la notificación: contenido, destinatarios, configuración y metadatos.
 * 
 * El diseño sigue el patrón Value Object: es inmutable y las comparaciones
 * se basan en el contenido, no en la referencia.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public final class NotificationRequest {
    
    /**
     * Contenido de la notificación
     */
    private final NotificationContent content;
    
    /**
     * Lista de destinatarios
     */
    @Builder.Default
    private final List<Recipient> recipients = new ArrayList<>();
    
    /**
     * Prioridad de la notificación
     */
    @Builder.Default
    private final Priority priority = Priority.NORMAL;
    
    /**
     * Metadatos adicionales para el procesamiento
     */
    private final Map<String, Object> metadata;
    
    /**
     * ID de correlación para tracking (trazabilidad)
     */
    private final String correlationId;
    
    /**
     * Timestamp de creación de la solicitud
     */
    @Builder.Default
    private final Instant createdAt = Instant.now();
    
    /**
     * Canal preferido para el envío (null = cualquier canal compatible)
     */
    private final ChannelType preferredChannel;
    
    /**
     * Opciones específicas de entrega
     */
    private final DeliveryOptions deliveryOptions;

    /**
     * Agrega un destinatario a la solicitud.
     *
     * @param recipient el destinatario a agregar
     * @return una nueva instancia de NotificationRequest con el destinatario agregado
     */
    public NotificationRequest addRecipient(Recipient recipient) {
        List<Recipient> updatedRecipients = new ArrayList<>(this.recipients);
        updatedRecipients.add(recipient);
        return NotificationRequest.builder()
            .content(this.content)
            .recipients(updatedRecipients)
            .priority(this.priority)
            .metadata(this.metadata)
            .correlationId(this.correlationId)
            .preferredChannel(this.preferredChannel)
            .deliveryOptions(this.deliveryOptions)
            .build();
    }
    
    /**
     * Obtiene el primer destinatario.
     *
     * @return el primer destinatario o null si no hay ninguno
     */
    public Recipient getPrimaryRecipient() {
        return recipients.isEmpty() ? null : recipients.get(0);
    }
    
    /**
     * Verifica si hay destinatarios.
     *
     * @return true si hay al menos un destinatario
     */
    public boolean hasRecipients() {
        return !recipients.isEmpty();
    }
    
    /**
     * Obtiene un valor de metadatos por clave.
     *
     * @param key la clave del metadato
     * @return el valor o null si no existe
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    /**
     * Define los niveles de prioridad para el envío.
     */
    public enum Priority {
        /** Prioridad baja - envío diferido */
        LOW,
        
        /** Prioridad normal - envío estándar */
        NORMAL,
        
        /** Prioridad alta - envío acelerado */
        HIGH,
        
        /** Prioridad urgente - envío inmediato */
        URGENT
    }
    
    /**
     * Opciones específicas de entrega.
     */
    @Getter
    @ToString
    @Builder
    public static final class DeliveryOptions {
        
        /**
         * Fecha/hora programada de entrega (null = entrega inmediata)
         */
        private final Instant scheduledAt;
        
        /**
         * Tiempo de vida de la notificación en segundos
         */
        @Builder.Default
        private final int timeToLiveSeconds = 86400; // 24 horas por defecto
        
        /**
         * Si es true, no guarda la notificación para reintentos
         */
        @Builder.Default
        private final boolean dontStore = false;
        
        /**
         * Si es true, requiere confirmación de entrega
         */
        @Builder.Default
        private final boolean requireDeliveryReceipt = false;
    }
}
