package org.notification;

import java.time.Duration;
import org.notification.config.NotificationConfig;

/**
 * API principal para el envío de notificaciones.
 * 
 * Esta fachada proporciona una interfaz unificada y simple para enviar
 * notificaciones a través de múltiples canales.
 * 
 * Ejemplo de uso:
 * <pre>
 * NotificationService service = NotificationService.builder()
 *     .withDefaultChannel(ChannelType.EMAIL)
 *     .build();
 * 
 * SendResult result = service.send(
 *     NotificationRequest.builder()
 *         .content(NotificationContent.withSubject("Hola", "Mensaje de prueba"))
 *         .addRecipient(Recipient.to("usuario@email.com"))
 *         .build()
 * );
 * </pre>
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public class NotificationService {
    
    private final NotificationConfig config;
    
    private NotificationService(NotificationConfig config) {
        this.config = config;
    }
    
    /**
     * Crea una instancia del builder para configurar el servicio.
     *
     * @return un nuevo builder
     */
    public static NotificationServiceBuilder builder() {
        return new NotificationServiceBuilder();
    }
    
    /**
     * Builder para NotificationService.
     */
    public static class NotificationServiceBuilder {
        
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private boolean enableLogging = true;
        private boolean enableMetrics = true;
        
        NotificationServiceBuilder() {
        }
        
        public NotificationServiceBuilder withDefaultTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public NotificationServiceBuilder withMaxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }
        
        public NotificationServiceBuilder enableLogging(boolean enable) {
            this.enableLogging = enable;
            return this;
        }
        
        public NotificationServiceBuilder enableMetrics(boolean enable) {
            this.enableMetrics = enable;
            return this;
        }
        
        public NotificationService build() {
            NotificationConfig config = NotificationConfig.builder()
                .globalTimeout(timeout)
                .maxRetries(maxRetries)
                .enableLogging(enableLogging)
                .enableMetrics(enableMetrics)
                .build();
            return new NotificationService(config);
        }
    }
}
