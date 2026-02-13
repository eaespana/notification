package org.notification.config.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.notification.channel.ChannelType;
import org.notification.config.NotificationConfig;

import java.util.Properties;

/**
 * Configuración específica para el canal de Email.
 * 
 * Esta clase encapsula todas las configuraciones necesarias para enviar
 * correos electrónicos a través de un proveedor SMTP.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public final class EmailChannelConfig implements NotificationConfig.ChannelConfig {
    
    /**
     * Servidor SMTP
     */
    private final String host;
    
    /**
     * Puerto SMTP
     */
    @Builder.Default
    private final int port = 587;
    
    /**
     * Usuario para autenticación
     */
    private final String username;
    
    /**
     * Contraseña/token para autenticación
     */
    private final String password;
    
    /**
     * Dirección de correo del remitente
     */
    private final String fromAddress;
    
    /**
     * Nombre del remitente
     */
    private final String fromName;
    
    /**
     * Habilitar TLS/SSL
     */
    @Builder.Default
    private final boolean useTls = true;
    
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
     * Propiedades adicionales para el Session de JavaMail
     */
    private final Properties additionalProperties;
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }
    
    /**
     * Crea las propiedades estándar para JavaMail.
     *
     * @return Properties configuradas para SMTP
     */
    public Properties toMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", connectionTimeoutMs);
        props.put("mail.smtp.timeout", readTimeoutMs);
        
        if (useTls) {
            props.put("mail.smtp.starttls.enable", "true");
        } else {
            props.put("mail.smtp.auth.disable", "true");
        }
        
        if (additionalProperties != null) {
            props.putAll(additionalProperties);
        }
        
        return props;
    }
}
