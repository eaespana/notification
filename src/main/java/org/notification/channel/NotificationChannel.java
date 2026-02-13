package org.notification.channel;

import org.notification.model.NotificationRequest;
import org.notification.model.SendResult;

/**
 * Interfaz base para todos los canales de notificación.
 * 
 * Esta interfaz define el contrato que debe cumplir cualquier implementación
 * de canal de notificación. El diseño sigue el patrón Strategy, permitiendo
 * que diferentes canales (Email, SMS, Push, etc.) se intercambien fácilmente.
 * 
 * Principios aplicados:
 * - ISP (Interface Segregation): Interfaz pequeña y enfocada
 * - DIP (Dependency Inversion): Las abstracciones no dependen de detalles
 * - OCP (Open/Closed): Nuevos canales sin modificar esta interfaz
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public interface NotificationChannel {
    
    /**
     * Envía una notificación a través de este canal.
     *
     * @param request la solicitud de notificación con contenido y destinatarios
     * @return el resultado del envío
     * @throws IllegalArgumentException si la solicitud es inválida
     * @throws org.notification.exceptionNotificationSendException si ocurre un error durante el envío
     */
    SendResult send(NotificationRequest request);
    
    /**
     * Retorna el tipo de canal que esta implementación representa.
     * 
     * Esta información es usada por el sistema para:
     * - Registro y descubrimiento de canales
     * - Routing de notificaciones
     * - Métricas y logging
     *
     * @return el ChannelType correspondiente a este canal
     */
    ChannelType getChannelType();
    
    /**
     * Verifica si este canal puede procesar la solicitud dada.
     * 
     * Algunos canales pueden requerir información específica en los metadatos
     * del request (ej. deviceToken para push, chatId para Telegram).
     * Este método permite al sistema determinar si el canal es compatible
     * con la solicitud antes de intentar el envío.
     *
     * @param request la solicitud a verificar
     * @return true si este canal puede procesar la solicitud
     */
    default boolean supports(NotificationRequest request) {
        // Por defecto, asumimos que el canal soporta cualquier solicitud
        // Las implementaciones específicas pueden sobrescribir este comportamiento
        return true;
    }
    
    /**
     * Retorna el nombre descriptivo del canal.
     * 
     * Útil para logging y debugging.
     *
     * @return nombre del canal
     */
    default String getChannelName() {
        return getChannelType().getIdentifier();
    }
    
    /**
     * Verifica si el canal está disponible para envíos.
     * 
     * Las implementaciones pueden usar este método para verificar
     * conectividad con el proveedor, credenciales, etc.
     *
     * @return true si el canal está disponible
     */
    default boolean isAvailable() {
        return true;
    }
}
