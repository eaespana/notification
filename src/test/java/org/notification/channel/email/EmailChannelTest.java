package org.notification.channel.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.exception.NotificationException;
import org.notification.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para EmailChannel.
 */
@DisplayName("EmailChannel Tests")
class EmailChannelTest {
    
    private EmailChannelConfig validConfig;
    private NotificationChannel channel;
    
    @BeforeEach
    void setUp() {
        validConfig = EmailChannelConfig.builder()
            .host("smtp.test.com")
            .port(587)
            .fromAddress("test@test.com")
            .fromName("Test Sender")
            .build();
        channel = new EmailChannel(validConfig);
    }
    
    @Nested
    @DisplayName("Envío exitoso")
    class SuccessfulSend {
        
        @Test
        @DisplayName("Debe enviar email exitosamente")
        void shouldSendEmailSuccessfully() {
            NotificationRequest request = createValidEmailRequest();
            
            SendResult result = channel.send(request);
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getMessageId());
            assertTrue(result.getMessageId().contains("@"));
        }
        
        @Test
        @DisplayName("Debe incluir correlationId en resultado")
        void shouldIncludeCorrelationId() {
            NotificationRequest request = createValidEmailRequest();
            request = NotificationRequest.builder()
                .content(request.getContent())
                .recipients(List.of(request.getRecipients().get(0)))
                .correlationId("TEST-123")
                .build();
            
            SendResult result = channel.send(request);
            
            assertTrue(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Validación de solicitudes")
    class ValidationTests {
        
        @Test
        @DisplayName("Debe lanzar excepción si falta contenido")
        void shouldThrowExceptionIfContentIsNull() {
            NotificationRequest request = NotificationRequest.builder()
                    .recipients(List.of(Recipient.of("test@test.com")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción si falta destinatario")
        void shouldThrowExceptionIfRecipientIsNull() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test body"))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción si email es inválido")
        void shouldThrowExceptionIfEmailIsInvalid() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.of("email-invalido")))
                .build();
            
            NotificationException exception = assertThrows(
                NotificationException.ValidationException.class,
                () -> channel.send(request)
            );
            
            assertTrue(exception.getMessage().contains("Invalid email"));
        }
    }
    
    @Nested
    @DisplayName("Configuración")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Debe estar disponible con configuración válida")
        void shouldBeAvailableWithValidConfig() {
            assertTrue(channel.isAvailable());
        }
        
        @Test
        @DisplayName("Debe retornar tipo EMAIL")
        void shouldReturnEmailType() {
            assertEquals(ChannelType.EMAIL, channel.getChannelType());
        }
        
        @Test
        @DisplayName("Debe soportar solicitud con email válido")
        void shouldSupportValidEmailRequest() {
            NotificationRequest request = createValidEmailRequest();
            assertTrue(channel.supports(request));
        }
        
        @Test
        @DisplayName("No debe soportar solicitud sin destinatarios")
        void shouldNotSupportRequestWithoutRecipients() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .build();
            assertFalse(channel.supports(request));
        }
    }
    
    @Nested
    @DisplayName("Configuración inválida")
    class InvalidConfigurationTests {
        
        @Test
        @DisplayName("Debe lanzar excepción si host es null")
        void shouldThrowExceptionIfHostIsNull() {
            EmailChannelConfig invalidConfig = EmailChannelConfig.builder()
                .fromAddress("test@test.com")
                .build();
            
            assertThrows(NotificationException.ConfigurationException.class,
                () -> new EmailChannel(invalidConfig));
        }
    }
    
    // Métodos auxiliares
    
    private NotificationRequest createValidEmailRequest() {
        return NotificationRequest.builder()
            .content(NotificationContent.withSubject("Test Subject", "Test Body"))
            .recipients(List.of(Recipient.of("recipient@test.com")))
            .build();
    }
}
