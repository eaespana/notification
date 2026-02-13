package org.notification.config.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.notification.channel.ChannelType;
import org.notification.config.NotificationConfig;

/**
 * Configuración específica para el canal de Push Notifications.
 * 
 * Esta clase encapsula todas las configuraciones necesarias para enviar
 * notificaciones push a través de proveedores como Firebase Cloud Messaging (FCM),
 * Apple APNs, etc.
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public final class PushChannelConfig implements NotificationConfig.ChannelConfig {
    
    /**
     * Project ID de Firebase o identificador del proyecto
     */
    private final String projectId;
    
    /**
     * Client email para autenticación (Firebase service account)
     */
    private final String clientEmail;
    
    /**
     * Private key para autenticación
     */
    private final String privateKey;
    
    /**
     * URL de la API de FCM
     */
    @Builder.Default
    private final String apiUrl = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    
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
     * Proveedor de push notifications
     */
    @Builder.Default
    private final PushProvider provider = PushProvider.FIREBASE;
    
    /**
     * Canal predeterminado para Android
     */
    @Builder.Default
    private final String defaultAndroidChannelId = "default";
    
    /**
     * Icono predeterminado para notificaciones
     */
    private final String defaultIcon;
    
    /**
     * Color predeterminado (formato #RRGGBB)
     */
    private final String defaultColor;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }
    
    /**
     * Proveedores de push soportados.
     */
    public enum PushProvider {
        FIREBASE,
        APNS,
        HUAWEI
    }
}
