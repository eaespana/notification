package org.notification.channel.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.config.channel.SmsChannelConfig;
import org.notification.exception.NotificationException;
import org.notification.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SmsChannel.
 */
@DisplayName("SmsChannel Tests")
class SmsChannelTest {
    
    private SmsChannelConfig validConfig;
    private NotificationChannel channel;
    
    @BeforeEach
    void setUp() {
        validConfig = SmsChannelConfig.builder()
            .fromNumber("+1234567890")
            .provider(SmsChannelConfig.SmsProvider.TWILIO)
            .build();
        channel = new SmsChannel(validConfig);
    }
    
    @Nested
    @DisplayName("Envío exitoso")
    class SuccessfulSend {
        
        @Test
        @DisplayName("Debe enviar SMS exitosamente")
        void shouldSendSmsSuccessfully() {
            NotificationRequest request = createValidSmsRequest();
            
            SendResult result = channel.send(request);
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getMessageId());
            assertTrue(result.getMessageId().startsWith("SM"));
        }
    }
    
    @Nested
    @DisplayName("Validación de solicitudes")
    class ValidationTests {
        
        @Test
        @DisplayName("Debe lanzar excepción si falta contenido")
        void shouldThrowExceptionIfContentIsNull() {
            NotificationRequest request = NotificationRequest.builder()
                .recipients(List.of(Recipient.of("+573001234567")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción si falta destinatario TO")
        void shouldThrowExceptionIfRecipientIsNotTo() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.cc("+573001234567")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción si número es inválido")
        void shouldThrowExceptionIfPhoneIsInvalid() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.of("numero-invalido")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe aceptar números internacionales")
        void shouldAcceptInternationalNumbers() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.of("+573001234567")))
                .build();
            
            SendResult result = channel.send(request);
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Debe aceptar números de 10 dígitos con prefijo")
        void shouldAccept10DigitNumbers() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.of("3001234567")))
                .build();
            
            SendResult result = channel.send(request);
            assertTrue(result.isSuccess());
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
        @DisplayName("Debe retornar tipo SMS")
        void shouldReturnSmsType() {
            assertEquals(ChannelType.SMS, channel.getChannelType());
        }
        
        @Test
        @DisplayName("Debe soportar solicitud con número válido")
        void shouldSupportValidPhoneRequest() {
            NotificationRequest request = createValidSmsRequest();
            assertTrue(channel.supports(request));
        }
        
        @Test
        @DisplayName("No debe soportar solicitud con email")
        void shouldNotSupportEmailRequest() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.of("test@email.com")))
                .build();
            assertFalse(channel.supports(request));
        }
    }
    
    @Nested
    @DisplayName("Configuración inválida")
    class InvalidConfigurationTests {
        
        @Test
        @DisplayName("Debe lanzar excepción si fromNumber es null")
        void shouldThrowExceptionIfFromNumberIsNull() {
            SmsChannelConfig invalidConfig = SmsChannelConfig.builder().build();
            
            assertThrows(NotificationException.ConfigurationException.class,
                () -> new SmsChannel(invalidConfig));
        }
    }
    
    // Métodos auxiliares
    
    private NotificationRequest createValidSmsRequest() {
        return NotificationRequest.builder()
            .content(NotificationContent.plainText("Tu código es 123456"))
            .recipients(List.of(Recipient.of("+573001234567")))
            .build();
    }
}
