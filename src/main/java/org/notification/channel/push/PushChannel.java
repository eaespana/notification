package org.notification.channel.push;

import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.config.channel.PushChannelConfig;
import org.notification.exception.NotificationException;
import org.notification.model.NotificationRequest;
import org.notification.model.Recipient;
import org.notification.model.SendResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación del canal de Push Notifications para el envío de notificaciones
 * a dispositivos móviles y web.
 * 
 * Esta clase simula el envío de notificaciones push mediante logging, permitiendo
 * probar la integración sin necesidad de un proveedor real como Firebase FCM.
 * 
 * Características del canal Push:
 * - Usa subject como title y body como body de la notificación
 * - Requiere device token en los metadatos del destinatario
 * - Soporta datos personalizados (data payload)
 * - Soporte para imágenes, badges, acciones
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public final class PushChannel implements NotificationChannel {
    
    private static final java.util.logging.Logger logger = 
        java.util.logging.Logger.getLogger(PushChannel.class.getName());
    
    private final PushChannelConfig config;
    
    /**
     * Constructor que recibe la configuración del canal.
     *
     * @param config la configuración específica de Push
     */
    public PushChannel(PushChannelConfig config) {
        this.config = config;
        validateConfiguration();
        logger.info("PushChannel initialized with provider: " + config.getProvider());
    }
    
    /**
     * Valida que la configuración tenga los campos requeridos.
     */
    private void validateConfiguration() {
        if (config.getProjectId() == null || config.getProjectId().isBlank()) {
            throw new NotificationException.ConfigurationException(
                "Project ID is required for Push channel"
            );
        }
    }
    
    @Override
    public SendResult send(NotificationRequest request) {
        logger.info("=== PUSH CHANNEL - Starting send process ===");
        logger.info("Request ID: " + request.getCorrelationId());
        
        // Validar la solicitud primero - lanzar excepción si es inválida
        validateRequest(request);
        
        try {
            // Enviar a cada destinatario
            int successCount = 0;
            for (Recipient recipient : request.getRecipients()) {
                if (recipient.getType() == org.notification.model.RecipientType.TO) {
                    String deviceToken = getDeviceToken(recipient);
                    if (deviceToken != null) {
                        simulateSendPush(request, recipient, deviceToken);
                        successCount++;
                    }
                }
            }
            
            // Generar ID único de mensaje
            String messageId = generateMessageId();
            
            logger.info("Push notification sent to " + successCount + " device(s). MessageId: " + messageId);
            logger.info("=== PUSH CHANNEL - Send process completed ===");
            
            return SendResult.success(messageId, "Push sent to " + successCount + " device(s)");
            
        } catch (Exception e) {
            logger.severe("Error sending push: " + e.getMessage());
            return SendResult.failure("Error sending push: " + e.getMessage());
        }
    }
    
    /**
     * Valida que la solicitud tenga los campos requeridos para Push.
     */
    private void validateRequest(NotificationRequest request) {
        if (request.getContent() == null) {
            throw new NotificationException.ValidationException("Push content is required");
        }
        
        // Para push, el body es obligatorio (es el mensaje principal)
        if (request.getContent().getBody() == null || request.getContent().getBody().isBlank()) {
            throw new NotificationException.ValidationException("Push body is required");
        }
        
        // Verificar que haya al menos un destinatario con device token
        boolean hasValidRecipient = request.getRecipients().stream()
            .anyMatch(r -> r.getType() == org.notification.model.RecipientType.TO 
                && hasValidDeviceToken(r));
        
        if (!hasValidRecipient) {
            throw new NotificationException.ValidationException(
                "At least one recipient with valid device token is required for Push"
            );
        }
    }
    
    /**
     * Verifica si el destinatario tiene un device token válido.
     */
    private boolean hasValidDeviceToken(Recipient recipient) {
        return getDeviceToken(recipient) != null;
    }
    
    /**
     * Extrae el device token de los metadatos del destinatario.
     */
    private String getDeviceToken(Recipient recipient) {
        if (recipient.getMetadata() == null) {
            return null;
        }
        
        // Soportar diferentes claves para device token
        Object token = recipient.getMetadata().get("deviceToken");
        if (token == null) {
            token = recipient.getMetadata().get("device_token");
        }
        if (token == null) {
            token = recipient.getMetadata().get("fcmToken");
        }
        if (token == null) {
            token = recipient.getMetadata().get("apnsToken");
        }
        
        return token != null ? token.toString() : null;
    }
    
    /**
     * Simula el envío de la notificación push mediante logging detallado.
     */
    private void simulateSendPush(NotificationRequest request, Recipient recipient, String deviceToken) {
        var content = request.getContent();
        
        logger.info("--- PUSH NOTIFICATION DETAILS ---");
        logger.info("Provider: " + config.getProvider());
        logger.info("Project ID: " + config.getProjectId());
        
        // Truncar device token para logging
        String truncatedToken = deviceToken.length() > 20 
            ? deviceToken.substring(0, 20) + "..." 
            : deviceToken;
        logger.info("Device Token: " + truncatedToken);
        
        // Title
        String title = content.getSubject();
        if (title != null && !title.isBlank()) {
            logger.info("Title: " + title);
        }
        
        // Body
        logger.info("Body: " + content.getBody());
        
        // Priority
        String priority = mapPriority(request.getPriority());
        logger.info("Priority: " + priority);
        
        // Image
        Object imageUrl = content.getMetadata() != null ? content.getMetadata().get("imageUrl") : null;
        if (imageUrl != null) {
            logger.info("Image URL: " + imageUrl);
        }
        
        // Badge
        Object badge = content.getMetadata() != null ? content.getMetadata().get("badge") : null;
        if (badge != null) {
            logger.info("Badge: " + badge);
        }
        
        // Custom data
        if (content.getMetadata() != null && !content.getMetadata().isEmpty()) {
            logger.info("Custom Data: " + sanitizeMetadata(content.getMetadata()));
        }
        
        logger.info("--- END PUSH NOTIFICATION DETAILS ---");
    }
    
    /**
     * Mapea la prioridad de la notificación a la del proveedor.
     */
    private String mapPriority(org.notification.model.NotificationRequest.Priority priority) {
        if (priority == null) {
            return "normal";
        }
        return switch (priority) {
            case URGENT, HIGH -> "high";
            case LOW -> "normal";
            case NORMAL -> "normal";
        };
    }
    
    /**
     * Sanitiza los metadatos para logging (remueve datos sensibles).
     */
    private Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey().toLowerCase();
            // Remover campos potencialmente sensibles
            if (key.contains("token") || key.contains("key") || key.contains("secret")) {
                sanitized.put(key, "***REDACTED***");
            } else {
                sanitized.put(key, entry.getValue());
            }
        }
        return sanitized;
    }
    
    /**
     * Genera un ID único de mensaje estilo FCM.
     */
    private String generateMessageId() {
        // Formato similar a los message IDs de FCM
        return "push_" + UUID.randomUUID().toString().replace("-", "").substring(0, 22);
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }
    
    @Override
    public boolean supports(NotificationRequest request) {
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            return false;
        }
        
        // Al menos un destinatario con device token válido
        return request.getRecipients().stream()
            .filter(r -> r.getType() == org.notification.model.RecipientType.TO)
            .anyMatch(this::hasValidDeviceToken);
    }
    
    @Override
    public String getChannelName() {
        return "PushChannel[" + config.getProvider() + ":" + config.getProjectId() + "]";
    }
    
    @Override
    public boolean isAvailable() {
        return config.getProjectId() != null && !config.getProjectId().isBlank();
    }
}
