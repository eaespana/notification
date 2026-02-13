package org.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.notification.model.SendResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SendResult.
 * 
 * Verifica el comportamiento del objeto de resultado de envío.
 */
@DisplayName("SendResult Tests")
class SendResultTest {
    
    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {
        
        @Test
        @DisplayName("success() debe crear resultado exitoso")
        void success_shouldCreateSuccessfulResult() {
            SendResult result = SendResult.success("msg-123");
            
            assertTrue(result.isSuccess());
            assertFalse(result.isFailure());
            assertFalse(result.shouldRetry());
            assertEquals("msg-123", result.getMessageId());
            assertEquals("Notification sent successfully", result.getMessage());
            assertEquals(SendResult.Status.SUCCESS, result.getStatus());
        }
        
        @Test
        @DisplayName("success() con mensaje personalizado")
        void success_withCustomMessage_shouldCreateSuccessfulResult() {
            SendResult result = SendResult.success("msg-456", "Email enviado");
            
            assertTrue(result.isSuccess());
            assertEquals("msg-456", result.getMessageId());
            assertEquals("Email enviado", result.getMessage());
        }
        
        @Test
        @DisplayName("failure() debe crear resultado fallido")
        void failure_shouldCreateFailedResult() {
            SendResult result = SendResult.failure("Error de conexión");
            
            assertFalse(result.isSuccess());
            assertTrue(result.isFailure());
            assertFalse(result.shouldRetry());
            assertEquals("Error de conexión", result.getMessage());
            assertEquals(SendResult.Status.FAILURE, result.getStatus());
        }
        
        @Test
        @DisplayName("retry() debe crear resultado que requiere reintento")
        void retry_shouldCreateRetryableResult() {
            SendResult result = SendResult.retry("Rate limit", 429);
            
            assertFalse(result.isSuccess());
            assertFalse(result.isFailure());
            assertTrue(result.shouldRetry());
            assertEquals("Rate limit", result.getMessage());
            assertEquals(429, result.getProviderCode());
            assertEquals(SendResult.Status.RETRY, result.getStatus());
        }
    }
    
    @Nested
    @DisplayName("Status Methods")
    class StatusMethods {
        
        @Test
        @DisplayName("isSuccess() debe retornar true solo para SUCCESS")
        void isSuccess_shouldReturnTrueOnlyForSuccess() {
            SendResult success = SendResult.success("id");
            SendResult failure = SendResult.failure("error");
            SendResult retry = SendResult.retry("retry", 429);
            
            assertTrue(success.isSuccess());
            assertFalse(failure.isSuccess());
            assertFalse(retry.isSuccess());
        }
        
        @Test
        @DisplayName("isFailure() debe retornar true solo para FAILURE")
        void isFailure_shouldReturnTrueOnlyForFailure() {
            SendResult success = SendResult.success("id");
            SendResult failure = SendResult.failure("error");
            SendResult retry = SendResult.retry("retry", 429);
            
            assertFalse(success.isFailure());
            assertTrue(failure.isFailure());
            assertFalse(retry.isFailure());
        }
        
        @Test
        @DisplayName("shouldRetry() debe retornar true solo para RETRY")
        void shouldRetry_shouldReturnTrueOnlyForRetry() {
            SendResult success = SendResult.success("id");
            SendResult failure = SendResult.failure("error");
            SendResult retry = SendResult.retry("retry", 429);
            
            assertFalse(success.shouldRetry());
            assertFalse(failure.shouldRetry());
            assertTrue(retry.shouldRetry());
        }
    }
    
    @Nested
    @DisplayName("Builder")
    class BuilderTests {
        
        @Test
        @DisplayName("Builder debe permitir crear resultado completo")
        void builder_shouldCreateCompleteResult() {
            SendResult result = SendResult.builder()
                .status(SendResult.Status.SUCCESS)
                .messageId("custom-id")
                .message("Custom message")
                .providerCode(200)
                .attempts(3)
                .build();
            
            assertTrue(result.isSuccess());
            assertEquals("custom-id", result.getMessageId());
            assertEquals("Custom message", result.getMessage());
            assertEquals(200, result.getProviderCode());
            assertEquals(3, result.getAttempts());
        }
        
        @Test
        @DisplayName("Builder debe usar valores por defecto")
        void builder_shouldUseDefaultValues() {
            SendResult result = SendResult.builder()
                .status(SendResult.Status.SUCCESS)
                .build();
            
            assertNotNull(result.getTimestamp());
            assertEquals(1, result.getAttempts());
        }
    }
}
