package org.notification.exception;

/**
 * Excepción base para todas las excepciones de la librería de notificaciones.
 * 
 * Esta jerarquía sigue el principio de diferenciación clara entre:
 * - Errores de validación: Problemas con los datos de entrada (recuperables corregibles)
 * - Errores de envío: Problemas al contactar al proveedor (pueden ser transitorios)
 * - Errores de configuración: Problemas de setup (requieren intervención)
 * 
 * Características:
 * - Código de error único para identificación programática
 * - Mensajes claros y accionables
 * - Causa original preservada
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public class NotificationException extends RuntimeException {
    
    private final String errorCode;
    private final ErrorCategory category;
    
    /**
     * Constructor con mensaje, código y categoría.
     */
    public NotificationException(String message, String errorCode, ErrorCategory category) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
    }
    
    /**
     * Constructor con mensaje, código, categoría y causa.
     */
    public NotificationException(String message, String errorCode, ErrorCategory category, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
    }
    
    /**
     * Constructor con solo mensaje (usa categoría genérica).
     */
    public NotificationException(String message) {
        this(message, "NOTIFICATION_ERROR", ErrorCategory.GENERAL);
    }
    
    /**
     * Constructor con mensaje y causa.
     */
    public NotificationException(String message, Throwable cause) {
        this(message, "NOTIFICATION_ERROR", ErrorCategory.GENERAL, cause);
    }
    
    /**
     * Obtiene el código de error.
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Obtiene la categoría del error.
     */
    public ErrorCategory getCategory() {
        return category;
    }
    
    /**
     * Indica si el error es recuperable (puede reintentarse).
     */
    public boolean isRecoverable() {
        return category == ErrorCategory.SEND_ERROR || category == ErrorCategory.RATE_LIMIT;
    }
    
    /**
     * Indica si el error es de validación.
     */
    public boolean isValidationError() {
        return category == ErrorCategory.VALIDATION;
    }
    
    /**
     * Categorías de error.
     */
    public enum ErrorCategory {
        /** Error general */
        GENERAL,
        
        /** Error de validación de entrada */
        VALIDATION,
        
        /** Error al enviar la notificación */
        SEND_ERROR,
        
        /** Error de rate limiting */
        RATE_LIMIT,
        
        /** Error de autenticación */
        AUTHENTICATION,
        
        /** Error de configuración */
        CONFIGURATION
    }
    
    // ==================== EXCEPCIONES ESPECÍFICAS ====================
    
    /**
     * Errores de validación de solicitudes.
     * 
     * Estos errores indican problemas con los datos de entrada que
     * deben corregirse antes de intentar enviar nuevamente.
     */
    public static class ValidationException extends NotificationException {
        
        public ValidationException(String message) {
            super(message, "VALIDATION_ERROR", ErrorCategory.VALIDATION);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, "VALIDATION_ERROR", ErrorCategory.VALIDATION, cause);
        }
        
        public ValidationException(String field, String message) {
            super(String.format("[%s] %s", field, message), "VALIDATION_ERROR", ErrorCategory.VALIDATION);
        }
    }
    
    /**
     * Errores durante el envío de notificaciones.
     * 
     * Pueden ser transitorios (problemas de red) o permanentes
     * (dirección inválida, etc.). Usar isRecoverable() para determinar.
     */
    public static class SendException extends NotificationException {
        
        private final String channelType;
        private final Integer providerCode;
        
        public SendException(String message, String channelType) {
            super(message, "SEND_ERROR", ErrorCategory.SEND_ERROR);
            this.channelType = channelType;
            this.providerCode = null;
        }
        
        public SendException(String message, String channelType, Integer providerCode) {
            super(message, "SEND_ERROR", ErrorCategory.SEND_ERROR);
            this.channelType = channelType;
            this.providerCode = providerCode;
        }
        
        public SendException(String message, String channelType, Throwable cause) {
            super(message, "SEND_ERROR", ErrorCategory.SEND_ERROR, cause);
            this.channelType = channelType;
            this.providerCode = null;
        }
        
        public String getChannelType() {
            return channelType;
        }
        
        public Integer getProviderCode() {
            return providerCode;
        }
    }
    
    /**
     * Errores de rate limiting.
     * 
     * Indica que se han agotado los límites del proveedor.
     * Son recuperables después del tiempo especificado.
     */
    public static class RateLimitException extends SendException {
        
        private final int retryAfterSeconds;
        
        public RateLimitException(String message, String channelType, int retryAfterSeconds) {
            super(message, channelType);
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        public int getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
        
        @Override
        public boolean isRecoverable() {
            return true;
        }
    }
    
    /**
     * Errores de autenticación con proveedores.
     * 
     * Generalmente no recuperables sin intervención (credenciales inválidas).
     */
    public static class AuthenticationException extends SendException {
        
        public AuthenticationException(String message, String channelType) {
            super(message, channelType);
        }
        
        public AuthenticationException(String message, String channelType, Throwable cause) {
            super(message, channelType, cause);
        }
    }
    
    /**
     * Errores de configuración.
     * 
     * Indican problemas con la configuración del sistema.
     * No son recuperables sin cambiar la configuración.
     */
    public static class ConfigurationException extends NotificationException {
        
        public ConfigurationException(String message) {
            super(message, "CONFIGURATION_ERROR", ErrorCategory.CONFIGURATION);
        }
        
        public ConfigurationException(String message, Throwable cause) {
            super(message, "CONFIGURATION_ERROR", ErrorCategory.CONFIGURATION, cause);
        }
    }
}
