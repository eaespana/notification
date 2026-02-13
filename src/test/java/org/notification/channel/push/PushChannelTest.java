package org.notification.channel.push;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.config.channel.PushChannelConfig;
import org.notification.exception.NotificationException;
import org.notification.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PushChannel.
 */
@DisplayName("PushChannel Tests")
class PushChannelTest {
    
    private PushChannelConfig validConfig;
    private NotificationChannel channel;
    
    @BeforeEach
    void setUp() {
        validConfig = PushChannelConfig.builder()
            .projectId("test-project")
            .provider(PushChannelConfig.PushProvider.FIREBASE)
            .build();
        channel = new PushChannel(validConfig);
    }
    
    @Nested
    @DisplayName("Envío exitoso")
    class SuccessfulSend {
        
        @Test
        @DisplayName("Debe enviar push exitosamente")
        void shouldSendPushSuccessfully() {
            NotificationRequest request = createValidPushRequest();
            
            SendResult result = channel.send(request);
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getMessageId());
            assertTrue(result.getMessageId().startsWith("push_"));
        }
        
        @Test
        @DisplayName("Debe enviar con múltiples destinatarios")
        void shouldSendToMultipleRecipients() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.withSubject("Title", "Body"))
                .recipients(List.of(
                    createRecipientWithToken("token1"),
                    createRecipientWithToken("token2")
                ))
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
                .recipients(List.of(Recipient.of("token123")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción si falta body")
        void shouldThrowExceptionIfBodyIsMissing() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.builder()
                    .subject("Title only")
                    .build())
                .recipients(List.of(Recipient.of("token123")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción si no hay device token")
        void shouldThrowExceptionIfNoDeviceToken() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(Recipient.of("user123")))
                .build();
            
            assertThrows(NotificationException.ValidationException.class, 
                () -> channel.send(request));
        }
        
        @Test
        @DisplayName("Debe aceptar diferentes formatos de token")
        void shouldAcceptDifferentTokenFormats() {
            // deviceToken
            NotificationRequest req1 = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(createRecipientWithToken("token", "deviceToken")))
                .build();
            assertTrue(channel.supports(req1));
            
            // fcmToken
            NotificationRequest req2 = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                .recipients(List.of(createRecipientWithToken("token", "fcmToken")))
                .build();
            assertTrue(channel.supports(req2));
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
        @DisplayName("Debe retornar tipo PUSH")
        void shouldReturnPushType() {
            assertEquals(ChannelType.PUSH, channel.getChannelType());
        }
        
        @Test
        @DisplayName("Debe soportar solicitud con device token válido")
        void shouldSupportValidTokenRequest() {
            NotificationRequest request = createValidPushRequest();
            assertTrue(channel.supports(request));
        }
        
        @Test
        @DisplayName("No debe soportar solicitud sin metadatos")
        void shouldNotSupportRequestWithoutMetadata() {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.plainText("Test"))
                    .recipients(List.of(Recipient.of("user")))
                .build();
            assertFalse(channel.supports(request));
        }
    }
    
    @Nested
    @DisplayName("Configuración inválida")
    class InvalidConfigurationTests {
        
        @Test
        @DisplayName("Debe lanzar excepción si projectId es null")
        void shouldThrowExceptionIfProjectIdIsNull() {
            PushChannelConfig invalidConfig = PushChannelConfig.builder().build();
            
            assertThrows(NotificationException.ConfigurationException.class,
                () -> new PushChannel(invalidConfig));
        }
    }
    
    // Métodos auxiliares
    
    private NotificationRequest createValidPushRequest() {
        return NotificationRequest.builder()
            .content(NotificationContent.withSubject("Title", "Body message"))
            .recipients(List.of(createRecipientWithToken("fcm_dummy_token_12345")))
            .build();
    }
    
    private Recipient createRecipientWithToken(String token) {
        return Recipient.builder()
            .identifier("user123")
            .type(RecipientType.TO)
            .metadata(Map.of("deviceToken", token))
            .build();
    }
    
    private Recipient createRecipientWithToken(String token, String key) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(key, token);
        return Recipient.builder()
            .identifier("user123")
            .type(RecipientType.TO)
            .metadata(metadata)
            .build();
    }
}
