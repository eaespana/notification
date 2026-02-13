package org.notification.channel.email;

import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.exception.NotificationException;
import org.notification.model.NotificationRequest;
import org.notification.model.Recipient;
import org.notification.model.SendResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación del canal de Email para el envío de notificaciones por correo electrónico.
 * 
 * Esta clase simula el envío de emails mediante logging, permitiendo probar la integración
 * sin necesidad de un servidor SMTP real.
 * 
 * Características del canal Email:
 * - Soporta subject, body y HTML
 * - Soporta múltiples destinatarios (TO, CC, BCC)
 * - Soporta adjuntos
 * - Simulación de envío con logging detallado
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public final class EmailChannel implements NotificationChannel {
    
    private static final java.util.logging.Logger logger = 
        java.util.logging.Logger.getLogger(EmailChannel.class.getName());
    
    private final EmailChannelConfig config;
    
    /**
     * Constructor que recibe la configuración del canal.
     *
     * @param config la configuración específica de email
     */
    public EmailChannel(EmailChannelConfig config) {
        this.config = config;
        validateConfiguration();
        logger.info("EmailChannel initialized with host: " + config.getHost());
    }
    
    /**
     * Valida que la configuración tenga los campos requeridos.
     */
    private void validateConfiguration() {
        if (config.getHost() == null || config.getHost().isBlank()) {
            throw new NotificationException.ConfigurationException(
                "Email host is required"
            );
        }
    }
    
    @Override
    public SendResult send(NotificationRequest request) {
        logger.info("=== EMAIL CHANNEL - Starting send process ===");
        logger.info("Request ID: " + request.getCorrelationId());
        
        // Validar la solicitud primero - lanzar excepción si es inválida
        validateRequest(request);
        
        try {
            // Simular envío del email
            simulateSendEmail(request);
            
            // Generar ID único de mensaje
            String messageId = generateMessageId();
            
            logger.info("Email sent successfully. MessageId: " + messageId);
            logger.info("=== EMAIL CHANNEL - Send process completed ===");
            
            return SendResult.success(messageId, "Email sent successfully");
            
        } catch (Exception e) {
            logger.severe("Error sending email: " + e.getMessage());
            return SendResult.failure("Error sending email: " + e.getMessage());
        }
    }
    
    /**
     * Valida que la solicitud tenga los campos requeridos para email.
     */
    private void validateRequest(NotificationRequest request) {
        if (request.getContent() == null) {
            throw new NotificationException.ValidationException("Email content is required");
        }
        
        if (request.getContent().getBody() == null || request.getContent().getBody().isBlank()) {
            throw new NotificationException.ValidationException("Email body is required");
        }
        
        if (request.getRecipients().isEmpty()) {
            throw new NotificationException.ValidationException(
                "At least one recipient is required for email"
            );
        }
        
        // Validar formato de emails
        for (Recipient recipient : request.getRecipients()) {
            if (!isValidEmail(recipient.getIdentifier())) {
                throw new NotificationException.ValidationException(
                    "Invalid email address: " + recipient.getIdentifier()
                );
            }
        }
    }
    
    /**
     * Simula el envío del email mediante logging detallado.
     */
    private void simulateSendEmail(NotificationRequest request) {
        var content = request.getContent();
        
        logger.info("--- EMAIL DETAILS ---");
        logger.info("From: " + config.getFromName() + " <" + config.getFromAddress() + ">");
        
        // Log de destinatarios por tipo
        request.getRecipients().stream()
            .filter(r -> r.getType() == org.notification.model.RecipientType.TO)
            .forEach(r -> logger.info("To: " + r.getIdentifier()));
        
        request.getRecipients().stream()
            .filter(r -> r.getType() == org.notification.model.RecipientType.CC)
            .forEach(r -> logger.info("Cc: " + r.getIdentifier()));
        
        request.getRecipients().stream()
            .filter(r -> r.getType() == org.notification.model.RecipientType.BCC)
            .forEach(r -> logger.info("Bcc: " + r.getIdentifier()));
        
        logger.info("Subject: " + (content.getSubject() != null ? content.getSubject() : "(no subject)"));
        logger.info("Body length: " + content.getBody().length() + " characters");
        
        if (content.getHtml() != null) {
            logger.info("HTML content available: " + content.getHtml().length() + " characters");
        }
        
        if (content.getAttachments() != null && !content.getAttachments().isEmpty()) {
            logger.info("Attachments: " + content.getAttachments().size());
            content.getAttachments().forEach(att -> 
                logger.info("  - " + att.getFilename() + " (" + att.getContentType() + ")")
            );
        }
        
        logger.info("SMTP Server: " + config.getHost() + ":" + config.getPort());
        logger.info("--- END EMAIL DETAILS ---");
    }
    
    /**
     * Genera un ID único de mensaje estilo Message-ID de email.
     */
    private String generateMessageId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String domain = config.getFromAddress() != null 
            ? config.getFromAddress().split("@")[1] 
            : "local";
        return "<" + uuid + "@" + domain + ">";
    }
    
    /**
     * Valida formato básico de email.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }
    
    @Override
    public boolean supports(NotificationRequest request) {
        // Email requiere destinatarios con emails válidos
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            return false;
        }
        
        return request.getRecipients().stream()
            .allMatch(r -> isValidEmail(r.getIdentifier()));
    }
    
    @Override
    public String getChannelName() {
        return "EmailChannel[" + config.getHost() + "]";
    }
    
    @Override
    public boolean isAvailable() {
        // En una implementación real, verificaríamos conexión con el servidor SMTP
        return config.getHost() != null && !config.getHost().isBlank();
    }
}
