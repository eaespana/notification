package org.notification.model;

/**
 * Define el tipo de destinatario en una notificación.
 * 
 * Esta enumeración sigue la convención de correo electrónico estándar:
 * - TO: Destinatario principal que debe recibir la notificación
 * - CC: Copia para conocimiento (todos ven quién la recibió)
 * - BCC/Cco: Copia oculta (los destinatarios no saben quién más la recibió)
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public enum RecipientType {
    /** Destinatario principal */
    TO,
    
    /** Copia con conocimiento */
    CC,
    
    /** Copia oculta */
    BCC
}
