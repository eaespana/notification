package org.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;

/**
 * Representa el resultado del intento de envío de una notificación.
 * 
 * Esta clase encapsula toda la información sobre el resultado del envío,
 * incluyendo el estado, identificador único del mensaje (si existe),
 * timestamp y metadatos adicionales.
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
public final class SendResult {
    
    /**
     * Estado del envío
     */
    private final Status status;
    
    /**
     * Identificador único del mensaje devuelto por el proveedor
     * (puede ser null si el envío falló antes de contactar al proveedor)
     */
    private final String messageId;
    
    /**
     * Timestamp del envío
     */
    @Builder.Default
    private final Instant timestamp = Instant.now();
    
    /**
     * Descripción legible del resultado (opcional)
     */
    private final String message;
    
    /**
     * Código de estado devuelto por el proveedor (HTTP status, código de API, etc.)
     */
    private final Integer providerCode;
    
    /**
     * Metadatos adicionales devueltos por el proveedor
     */
    private final Map<String, Object> metadata;
    
    /**
     * Número de intentos realizados (útil para reintentos)
     */
    @Builder.Default
    private final int attempts = 1;
    
    /**
     * Crea un resultado de envío exitoso.
     *
     * @param messageId el identificador del mensaje
     * @return una instancia de SendResult con estado SUCCESS
     */
    public static SendResult success(String messageId) {
        return SendResult.builder()
            .status(Status.SUCCESS)
            .messageId(messageId)
            .message("Notification sent successfully")
            .build();
    }
    
    /**
     * Crea un resultado de envío exitoso con mensaje personalizado.
     *
     * @param messageId el identificador del mensaje
     * @param message   la descripción del resultado
     * @return una instancia de SendResult con estado SUCCESS
     */
    public static SendResult success(String messageId, String message) {
        return SendResult.builder()
            .status(Status.SUCCESS)
            .messageId(messageId)
            .message(message)
            .build();
    }
    
    /**
     * Crea un resultado de envío fallido.
     *
     * @param errorMessage la descripción del error
     * @return una instancia de SendResult con estado FAILURE
     */
    public static SendResult failure(String errorMessage) {
        return SendResult.builder()
            .status(Status.FAILURE)
            .message(errorMessage)
            .build();
    }
    
    /**
     * Crea un resultado de envío que requiere reintento.
     *
     * @param reason       la razón del reintento
     * @param providerCode el código devuelto por el proveedor
     * @return una instancia de SendResult con estado RETRY
     */
    public static SendResult retry(String reason, Integer providerCode) {
        return SendResult.builder()
            .status(Status.RETRY)
            .message(reason)
            .providerCode(providerCode)
            .build();
    }
    
    /**
     * Indica si el envío fue exitoso.
     *
     * @return true si el estado es SUCCESS
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    /**
     * Indica si el envío falló permanentemente.
     *
     * @return true si el estado es FAILURE
     */
    public boolean isFailure() {
        return status == Status.FAILURE;
    }
    
    /**
     * Indica si el envío debe ser reintentado.
     *
     * @return true si el estado es RETRY
     */
    public boolean shouldRetry() {
        return status == Status.RETRY;
    }
    
    /**
     * Define los posibles estados de un envío de notificación.
     */
    public enum Status {
        /** El envío fue exitoso */
        SUCCESS,
        
        /** El envío falló permanentemente */
        FAILURE,
        
        /** El envío debe ser reintentado */
        RETRY
    }
}
