package org.notification.exception;

import org.notification.channel.NotificationChannel;
import org.notification.channel.email.EmailChannel;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.model.*;
import org.notification.model.NotificationRequest.Priority;

import java.util.List;
import java.util.Optional;

/**
 * Ejemplos de manejo de errores en la librería de notificaciones.
 * 
 * Este archivo demuestra las diferentes estrategias de manejo de errores
 * incluyendo try-catch, validación, y reintentos.
 * 
 * @author Notification Library
 */
public class ErrorHandlingExamples {
    
    /**
     * Ejemplo 1: Manejo básico con try-catch.
     */
    public static void basicErrorHandling() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 1: MANEJO BÁSICO CON TRY-CATCH");
        System.out.println("=".repeat(60));
        System.out.println();
        
        NotificationChannel channel = createEmailChannel();
        NotificationRequest request = createBasicRequest();
        
        try {
            SendResult result = channel.send(request);
            
            if (result.isSuccess()) {
                System.out.println("✓ Email enviado exitosamente");
                System.out.println("  Message ID: " + result.getMessageId());
            } else if (result.shouldRetry()) {
                System.out.println("⚠ El envío debe ser reintentado");
                System.out.println("  Motivo: " + result.getMessage());
            } else {
                System.out.println("✗ El envío falló");
                System.out.println("  Motivo: " + result.getMessage());
            }
            
        } catch (NotificationException e) {
            System.out.println("✗ Excepción capturada:");
            System.out.println("  Código: " + e.getErrorCode());
            System.out.println("  Categoría: " + e.getCategory());
            System.out.println("  Mensaje: " + e.getMessage());
            System.out.println("  Es recuperable: " + e.isRecoverable());
        }
        
        System.out.println();
    }
    
    /**
     * Ejemplo 2: Diferenciación entre errores de validación y envío.
     */
    public static void validationVsSendErrors() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 2: DIFERENCIACIÓN DE ERRORES");
        System.out.println("=".repeat(60));
        System.out.println();
        
        NotificationChannel channel = createEmailChannel();
        
        // Caso 1: Error de validación (datos inválidos)
        System.out.println("--- Caso 1: Error de validación ---");
        try {
            NotificationRequest invalidRequest = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                // Falta destinatario
                .build();
            
            channel.send(invalidRequest);
            
        } catch (NotificationException.ValidationException e) {
            System.out.println("✗ Error de validación:");
            System.out.println("  Código: " + e.getErrorCode());
            System.out.println("  Es recuperable: " + e.isRecoverable());
            System.out.println("  Acción: Corregir los datos de entrada");
        }
        
        System.out.println();
        
        // Caso 2: Error de autenticación
        System.out.println("--- Caso 2: Error de autenticación ---");
        try {
            NotificationChannel authFailingChannel = createEmailChannelWithInvalidCredentials();
            NotificationRequest request = createBasicRequest();
            
            authFailingChannel.send(request);
            
        } catch (NotificationException.AuthenticationException e) {
            System.out.println("✗ Error de autenticación:");
            System.out.println("  Código: " + e.getErrorCode());
            System.out.println("  Canal: " + e.getChannelType());
            System.out.println("  Es recuperable: " + e.isRecoverable());
            System.out.println("  Acción: Verificar credenciales");
        } catch (NotificationException e) {
            System.out.println("Otro error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Ejemplo 3: Manejo de rate limiting.
     */
    public static void rateLimitHandling() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 3: MANEJO DE RATE LIMITING");
        System.out.println("=".repeat(60));
        System.out.println();
        
        NotificationChannel channel = createEmailChannel();
        NotificationRequest request = createBasicRequest();
        
        try {
            SendResult result = channel.send(request);
            
            if (result.shouldRetry()) {
                System.out.println("⚠ Rate limit alcanzado");
                
                // En un caso real, esperar el tiempo especificado
                if (result.getMetadata() != null) {
                    Object retryAfter = result.getMetadata().get("retryAfter");
                    System.out.println("  Esperar: " + retryAfter + " segundos");
                }
            }
            
        } catch (NotificationException.RateLimitException e) {
            System.out.println("⚠ Rate limit alcanzado:");
            System.out.println("  Canal: " + e.getChannelType());
            System.out.println("  Esperar: " + e.getRetryAfterSeconds() + " segundos");
            System.out.println("  Es recuperable: " + e.isRecoverable());
        }
        
        System.out.println();
    }
    
    /**
     * Ejemplo 4: Validación proactiva.
     */
    public static void proactiveValidation() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 4: VALIDACIÓN PROACTIVA");
        System.out.println("=".repeat(60));
        System.out.println();
        
        NotificationChannel channel = createEmailChannel();
        
        // Validar antes de enviar
        NotificationRequest request = createBasicRequest();
        
        // Verificar si el canal soporta la solicitud
        if (!channel.supports(request)) {
            System.out.println("⚠ El canal no soporta esta solicitud");
            System.out.println("  Acción: Usar otro canal o ajustar la solicitud");
            return;
        }
        
        // Validar manualmente datos específicos
        if (request.getRecipients().isEmpty()) {
            System.out.println("⚠ No hay destinatarios");
            return;
        }
        
        String email = request.getRecipients().get(0).getIdentifier();
        if (!isValidEmail(email)) {
            System.out.println("⚠ Email inválido: " + email);
            return;
        }
        
        System.out.println("✓ Validación pasada, enviando...");
        SendResult result = channel.send(request);
        System.out.println("  Resultado: " + (result.isSuccess() ? "OK" : result.getMessage()));
        
        System.out.println();
    }
    
    /**
     * Ejemplo 5: Patrón de reintentos.
     */
    public static void retryPattern() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 5: PATRÓN DE REINTENTOS");
        System.out.println("=".repeat(60));
        System.out.println();
        
        NotificationChannel channel = createEmailChannel();
        NotificationRequest request = createBasicRequest();
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            attempt++;
            System.out.println("Intento " + attempt + " de " + maxRetries);
            
            try {
                SendResult result = channel.send(request);
                
                if (result.isSuccess()) {
                    System.out.println("✓ Envío exitoso en intento " + attempt);
                    return;
                } else if (result.shouldRetry()) {
                    System.out.println("  Falló, reintentando...");
                    try {
                        Thread.sleep(1000 * attempt); // Backoff simple
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    System.out.println("✗ Error permanente: " + result.getMessage());
                    return;
                }
                
            } catch (NotificationException e) {
                System.out.println("  Excepción: " + e.getMessage());
                
                if (!e.isRecoverable()) {
                    System.out.println("  Error no recuperable, deteniendo reintentos");
                    return;
                }
                
                if (attempt < maxRetries) {
                    System.out.println("  Reintentando...");
                }
            }
        }
        
        System.out.println("✗ Todos los reintentos fallaron");
        System.out.println();
    }
    
    /**
     * Ejemplo 6: Manejo estructurado con categorización.
     */
    public static void structuredErrorHandling() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 6: MANEJO ESTRUCTURADO");
        System.out.println("=".repeat(60));
        System.out.println();
        
        NotificationChannel channel = createEmailChannel();
        
        processNotification(channel, createBasicRequest());
        
        System.out.println();
    }
    
    /**
     * Procesa una notificación con manejo estructurado de errores.
     */
    private static void processNotification(NotificationChannel channel, NotificationRequest request) {
        try {
            SendResult result = channel.send(request);
            
            if (result.isSuccess()) {
                handleSuccess(result);
            } else {
                handleFailure(result);
            }
            
        } catch (NotificationException.ValidationException e) {
            handleValidationError(e);
        } catch (NotificationException.AuthenticationException e) {
            handleAuthError(e);
        } catch (NotificationException.RateLimitException e) {
            handleRateLimitError(e);
        } catch (NotificationException.ConfigurationException e) {
            handleConfigError(e);
        } catch (NotificationException e) {
            handleGenericError(e);
        }
    }
    
    private static void handleSuccess(SendResult result) {
        System.out.println("✓ Éxito: " + result.getMessageId());
    }
    
    private static void handleFailure(SendResult result) {
        System.out.println("✗ Fallo: " + result.getMessage());
    }
    
    private static void handleValidationError(NotificationException.ValidationException e) {
        System.out.println("✗ Error de validación:");
        System.out.println("  Código: " + e.getErrorCode());
        System.out.println("  Mensaje: " + e.getMessage());
        System.out.println("  Acción: Corregir datos de entrada");
    }
    
    private static void handleAuthError(NotificationException.AuthenticationException e) {
        System.out.println("✗ Error de autenticación:");
        System.out.println("  Canal: " + e.getChannelType());
        System.out.println("  Acción: Verificar API keys y credenciales");
    }
    
    private static void handleRateLimitError(NotificationException.RateLimitException e) {
        System.out.println("⚠ Rate limit:");
        System.out.println("  Esperar: " + e.getRetryAfterSeconds() + " segundos");
        System.out.println("  Acción: Implementar backoff");
    }
    
    private static void handleConfigError(NotificationException.ConfigurationException e) {
        System.out.println("✗ Error de configuración:");
        System.out.println("  Mensaje: " + e.getMessage());
        System.out.println("  Acción: Revisar configuración");
    }
    
    private static void handleGenericError(NotificationException e) {
        System.out.println("✗ Error:");
        System.out.println("  Categoría: " + e.getCategory());
        System.out.println("  Mensaje: " + e.getMessage());
        System.out.println("  Recuperable: " + e.isRecoverable());
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private static NotificationChannel createEmailChannel() {
        EmailChannelConfig config = EmailChannelConfig.builder()
            .host("smtp.test.com")
            .fromAddress("test@test.com")
            .build();
        return new EmailChannel(config);
    }
    
    private static NotificationChannel createEmailChannelWithInvalidCredentials() {
        EmailChannelConfig config = EmailChannelConfig.builder()
            .host("smtp.invalid.com")
            .fromAddress("invalid@test.com")
            .username("wrong")
            .password("wrong")
            .build();
        return new EmailChannel(config);
    }
    
    private static NotificationRequest createBasicRequest() {
        return NotificationRequest.builder()
            .content(NotificationContent.withSubject("Test", "Mensaje de prueba"))
            .recipients(List.of(Recipient.of("test@example.com")))
            .priority(Priority.NORMAL)
            .correlationId("TEST-001")
            .build();
    }
    
    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Método principal para ejecutar ejemplos.
     */
    public static void main(String[] args) {
        System.out.println("\n" + "#".repeat(60));
        System.out.println("# EJEMPLOS DE MANEJO DE ERRORES");
        System.out.println("#".repeat(60) + "\n");
        
        basicErrorHandling();
        validationVsSendErrors();
        rateLimitHandling();
        proactiveValidation();
        retryPattern();
        structuredErrorHandling();
        
        System.out.println("#".repeat(60));
        System.out.println("# TODOS LOS EJEMPLOS COMPLETADOS");
        System.out.println("#".repeat(60));
    }
}
