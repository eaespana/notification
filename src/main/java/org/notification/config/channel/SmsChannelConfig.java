package org.notification.config.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.notification.channel.ChannelType;
import org.notification.config.NotificationConfig;

/**
 * Configuración específica para el canal de SMS.
 * 
 * Esta clase encapsula todas las configuraciones necesarias para enviar
 * mensajes SMS a través de un proveedor como Twilio, AWS SNS, etc.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public final class SmsChannelConfig implements NotificationConfig.ChannelConfig {
    
    /**
     * Account SID o identificador de cuenta del proveedor
     */
    private final String accountSid;
    
    /**
     * Token de autenticación
     */
    private final String authToken;
    
    /**
     * Número de teléfono remitente (From number)
     */
    private final String fromNumber;
    
    /**
     * URL base de la API del proveedor
     */
    @Builder.Default
    private final String apiUrl = "https://api.twilio.com/2010-04-01";
    
    /**
     * Timeout de conexión en milisegundos
     */
    @Builder.Default
    private final int connectionTimeoutMs = 5000;
    
    /**
     * Timeout de lectura en milisegundos
     */
    @Builder.Default
    private final int readTimeoutMs = 10000;
    
    /**
     * Máximo de caracteres por SMS (estándar GSM es 160)
     */
    @Builder.Default
    private final int maxCharactersPerSms = 160;
    
    /**
     * Proveedor de SMS (TWILIO, AWS_SNS, NEXMO, CUSTOM)
     */
    @Builder.Default
    private final SmsProvider provider = SmsProvider.TWILIO;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }
    
    /**
     * Proveedores de SMS soportados.
     */
    public enum SmsProvider {
        TWILIO,
        AWS_SNS,
        NEXMO,
        CUSTOM
    }
}
