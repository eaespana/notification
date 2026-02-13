package org.notification.channel.sms;

import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.config.channel.SmsChannelConfig;
import org.notification.exception.NotificationException;
import org.notification.model.NotificationRequest;
import org.notification.model.Recipient;
import org.notification.model.SendResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación del canal de SMS para el envío de notificaciones por mensaje de texto.
 * 
 * Esta clase simula el envío de SMS mediante logging, permitiendo probar la integración
 * sin necesidad de un proveedor real como Twilio o AWS SNS.
 * 
 * Características del canal SMS:
 * - Solo usa el cuerpo del mensaje (subject ignorado)
 * - Limita el mensaje a 160 caracteres por SMS estándar
 * - Solo considera destinatarios TO (CC y BCC ignorados para SMS)
 * - Soporta números internacionales
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public final class SmsChannel implements NotificationChannel {
    
    private static final java.util.logging.Logger logger = 
        java.util.logging.Logger.getLogger(SmsChannel.class.getName());
    
    private final SmsChannelConfig config;
    
    /**
     * Constructor que recibe la configuración del canal.
     *
     * @param config la configuración específica de SMS
     */
    public SmsChannel(SmsChannelConfig config) {
        this.config = config;
        validateConfiguration();
        logger.info("SmsChannel initialized with provider: " + config.getProvider());
    }
    
    /**
     * Valida que la configuración tenga los campos requeridos.
     */
    private void validateConfiguration() {
        if (config.getFromNumber() == null || config.getFromNumber().isBlank()) {
            throw new NotificationException.ConfigurationException(
                "From number is required for SMS channel"
            );
        }
    }
    
    @Override
    public SendResult send(NotificationRequest request) {
        logger.info("=== SMS CHANNEL - Starting send process ===");
        logger.info("Request ID: " + request.getCorrelationId());
        
        // Validar la solicitud primero - lanzar excepción si es inválida
        validateRequest(request);
        
        try {
            // Procesar y enviar SMS
            for (Recipient recipient : request.getRecipients()) {
                if (recipient.getType() == org.notification.model.RecipientType.TO) {
                    simulateSendSms(request, recipient);
                }
            }
            
            // Generar ID único de mensaje
            String messageId = generateMessageId();
            
            logger.info("SMS sent successfully. MessageId: " + messageId);
            logger.info("=== SMS CHANNEL - Send process completed ===");
            
            return SendResult.success(messageId, "SMS sent successfully");
            
        } catch (Exception e) {
            logger.severe("Error sending SMS: " + e.getMessage());
            return SendResult.failure("Error sending SMS: " + e.getMessage());
        }
    }
    
    /**
     * Valida que la solicitud tenga los campos requeridos para SMS.
     */
    private void validateRequest(NotificationRequest request) {
        if (request.getContent() == null) {
            throw new NotificationException.ValidationException("SMS content is required");
        }
        
        if (request.getContent().getBody() == null || request.getContent().getBody().isBlank()) {
            throw new NotificationException.ValidationException("SMS body is required");
        }
        
        // Verificar que haya al menos un destinatario TO
        boolean hasToRecipient = request.getRecipients().stream()
            .anyMatch(r -> r.getType() == org.notification.model.RecipientType.TO);
        
        if (!hasToRecipient) {
            throw new NotificationException.ValidationException(
                "At least one TO recipient is required for SMS"
            );
        }
        
        // Validar formato de números de teléfono
        for (Recipient recipient : request.getRecipients()) {
            if (recipient.getType() == org.notification.model.RecipientType.TO) {
                if (!isValidPhoneNumber(recipient.getIdentifier())) {
                    throw new NotificationException.ValidationException(
                        "Invalid phone number: " + recipient.getIdentifier()
                    );
                }
            }
        }
        
        // Verificar límite de caracteres
        int bodyLength = request.getContent().getBody().length();
        if (bodyLength > config.getMaxCharactersPerSms()) {
            logger.warning("Message body exceeds max characters. Will be split into multiple SMS");
            logger.warning("Body length: " + bodyLength + ", Max: " + config.getMaxCharactersPerSms());
        }
    }
    
    /**
     * Simula el envío del SMS mediante logging detallado.
     */
    private void simulateSendSms(NotificationRequest request, Recipient recipient) {
        var content = request.getContent();
        
        // Truncar o dividir mensaje si es necesario
        String message = content.getBody();
        int maxChars = config.getMaxCharactersPerSms();
        
        logger.info("--- SMS DETAILS ---");
        logger.info("From: " + config.getFromNumber());
        logger.info("To: " + formatPhoneNumber(recipient.getIdentifier()));
        logger.info("Provider: " + config.getProvider());
        logger.info("API URL: " + config.getApiUrl());
        
        if (message.length() <= maxChars) {
            logger.info("Message: " + message);
            logger.info("Characters: " + message.length());
            logger.info("Parts: 1 SMS");
        } else {
            int parts = (int) Math.ceil((double) message.length() / maxChars);
            logger.info("Message (truncated): " + message.substring(0, Math.min(50, message.length())) + "...");
            logger.info("Characters: " + message.length());
            logger.info("Parts: " + parts + " SMS segments");
        }
        
        // Si hay metadata, loguearla
        if (content.getMetadata() != null && !content.getMetadata().isEmpty()) {
            logger.info("Metadata: " + content.getMetadata());
        }
        
        logger.info("--- END SMS DETAILS ---");
    }
    
    /**
     * Genera un ID único de mensaje estilo SID de Twilio.
     */
    private String generateMessageId() {
        // Estilo SMxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        return "SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
    
    /**
     * Valida formato básico de número de teléfono internacional.
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        // Aceptar formatos: +1234567890, 1234567890, 00-123-456-7890
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return cleaned.matches("^\\+?[0-9]{7,15}$");
    }
    
    /**
     * Formatea el número de teléfono para mostrar.
     */
    private String formatPhoneNumber(String phone) {
        if (phone.startsWith("+")) {
            return phone;
        }
        if (phone.length() == 10) {
            return "+1" + phone;
        }
        return "+" + phone;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }
    
    @Override
    public boolean supports(NotificationRequest request) {
        // SMS requiere destinatarios TO con números válidos
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            return false;
        }
        
        return request.getRecipients().stream()
            .filter(r -> r.getType() == org.notification.model.RecipientType.TO)
            .anyMatch(r -> isValidPhoneNumber(r.getIdentifier()));
    }
    
    @Override
    public String getChannelName() {
        return "SmsChannel[" + config.getProvider() + ":" + config.getFromNumber() + "]";
    }
    
    @Override
    public boolean isAvailable() {
        return config.getFromNumber() != null && !config.getFromNumber().isBlank();
    }
}
