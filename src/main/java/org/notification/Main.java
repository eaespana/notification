package org.notification;

import org.notification.channel.NotificationChannel;
import org.notification.channel.email.EmailChannel;
import org.notification.channel.push.PushChannel;
import org.notification.channel.sms.SmsChannel;
import org.notification.config.NotificationConfig;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.config.channel.PushChannelConfig;
import org.notification.config.channel.SmsChannelConfig;
import org.notification.model.*;
import org.notification.model.NotificationRequest.Priority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejemplos de uso de la librería de notificaciones.
 * 
 * Este archivo demuestra cómo utilizar los tres canales implementados:
 * - Email
 * - SMS
 * - Push Notification
 * 
 * @author Notification Library
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("LIBRERÍA DE NOTIFICACIONES - EJEMPLOS DE USO");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Ejemplo 1: Envío de Email
        exampleEmailChannel();
        
        // Ejemplo 2: Envío de SMS
        exampleSmsChannel();
        
        // Ejemplo 3: Envío de Push Notification
        examplePushChannel();
        
        // Ejemplo 4: Configuración global
        exampleGlobalConfiguration();
        
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("TODOS LOS EJEMPLOS COMPLETADOS");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Ejemplo de envío de Email con todas las características.
     */
    private static void exampleEmailChannel() {
        System.out.println("-".repeat(60));
        System.out.println("EJEMPLO 1: ENVÍO DE EMAIL");
        System.out.println("-".repeat(60));
        System.out.println();
        
        // Configurar el canal de Email
        EmailChannelConfig emailConfig = EmailChannelConfig.builder()
            .host("smtp.sendgrid.net")
            .port(587)
            .username("apikey")
            .password("SG.xxxxxxxx")
            .fromAddress("noreply@miempresa.com")
            .fromName("Mi Empresa")
            .useTls(true)
            .build();
        
        // Crear el canal
        NotificationChannel emailChannel = new EmailChannel(emailConfig);
        
        // Crear el contenido del email
        NotificationContent content = NotificationContent.builder()
            .subject("Bienvenido a Mi Empresa")
            .body("Hola Juan,\n\nGracias por registrarte en nuestra plataforma.\n\nSaludos,\nEl equipo de Mi Empresa")
            .html("<html><body><h1>Bienvenido Juan</h1><p>Gracias por registrarte en nuestra plataforma.</p></body></html>")
            .build();
        
        // Crear destinatarios
        Recipient recipient = Recipient.builder()
            .identifier("juan.perez@email.com")
            .displayName("Juan Pérez")
            .type(RecipientType.TO)
            .build();
        
        // Crear solicitud de notificación
        NotificationRequest request = NotificationRequest.builder()
            .content(content)
            .recipients(List.of(recipient, Recipient.builder()
                .identifier("manager@empresa.com")
                .type(RecipientType.CC)
                .build()))
            .priority(Priority.NORMAL)
            .correlationId("CORR-001-EMAIL")
            .build();
        
        // Enviar
        System.out.println("Enviando email...");
        SendResult result = emailChannel.send(request);
        
        // Verificar resultado
        System.out.println();
        System.out.println("RESULTADO:");
        System.out.println("  Éxito: " + result.isSuccess());
        System.out.println("  Message ID: " + result.getMessageId());
        System.out.println("  Mensaje: " + result.getMessage());
        System.out.println();
    }
    
    /**
     * Ejemplo de envío de SMS.
     */
    private static void exampleSmsChannel() {
        System.out.println("-".repeat(60));
        System.out.println("EJEMPLO 2: ENVÍO DE SMS");
        System.out.println("-".repeat(60));
        System.out.println();
        
        // Configurar el canal de SMS
        SmsChannelConfig smsConfig = SmsChannelConfig.builder()
            .accountSid("ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
            .authToken("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
            .fromNumber("+1234567890")
            .provider(SmsChannelConfig.SmsProvider.TWILIO)
            .maxCharactersPerSms(160)
            .build();
        
        // Crear el canal
        NotificationChannel smsChannel = new SmsChannel(smsConfig);
        
        // Crear contenido (solo body, sin subject para SMS)
        NotificationContent content = NotificationContent.builder()
            .body("Tu código de verificación es: 123456. Expira en 5 minutos.")
            .metadata(Map.of(
                "type", "verification_code",
                "expires_in", "300"
            ))
            .build();
        
        // Crear destinatario con número de teléfono
        Recipient recipient = Recipient.builder()
            .identifier("+573001234567")
            .displayName("Juan Pérez")
            .type(RecipientType.TO)
            .build();
        
        // Crear solicitud
        NotificationRequest request = NotificationRequest.builder()
            .content(content)
            .recipients(List.of(recipient))
            .priority(Priority.HIGH)
            .correlationId("CORR-002-SMS")
            .build();
        
        // Enviar
        System.out.println("Enviando SMS...");
        SendResult result = smsChannel.send(request);
        
        // Verificar resultado
        System.out.println();
        System.out.println("RESULTADO:");
        System.out.println("  Éxito: " + result.isSuccess());
        System.out.println("  Message ID: " + result.getMessageId());
        System.out.println("  Mensaje: " + result.getMessage());
        System.out.println();
    }
    
    /**
     * Ejemplo de envío de Push Notification.
     */
    private static void examplePushChannel() {
        System.out.println("-".repeat(60));
        System.out.println("EJEMPLO 3: ENVÍO DE PUSH NOTIFICATION");
        System.out.println("-".repeat(60));
        System.out.println();
        
        // Configurar el canal de Push
        PushChannelConfig pushConfig = PushChannelConfig.builder()
            .projectId("mi-proyecto-firebase")
            .clientEmail("firebase-adminsdk@mi-proyecto.iam.gserviceaccount.com")
            .privateKey("-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----")
            .provider(PushChannelConfig.PushProvider.FIREBASE)
            .defaultAndroidChannelId("notificaciones_generales")
            .defaultIcon("ic_notification")
            .defaultColor("#FF5722")
            .build();
        
        // Crear el canal
        NotificationChannel pushChannel = new PushChannel(pushConfig);
        
        // Crear contenido
        NotificationContent content = NotificationContent.builder()
            .subject("Nueva promoción!")
            .body("Tienes un 20% de descuento en tu próxima compra. ¡Apúrate!")
            .metadata(Map.of(
                "promo_id", "PROMO-2024-001",
                "discount", "20",
                "imageUrl", "https://miempresa.com/promo.jpg",
                "action", "open_promo",
                "badge", 1
            ))
            .build();
        
        // Crear destinatario con device token
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deviceToken", "fcm_dummy_token_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        metadata.put("platform", "android");
        
        Recipient recipient = Recipient.builder()
            .identifier("user_12345")
            .displayName("Juan Pérez")
            .type(RecipientType.TO)
            .metadata(metadata)
            .build();
        
        // Crear solicitud
        NotificationRequest request = NotificationRequest.builder()
            .content(content)
            .recipients(List.of(recipient))
            .priority(Priority.URGENT)
            .correlationId("CORR-003-PUSH")
            .build();
        
        // Enviar
        System.out.println("Enviando Push Notification...");
        SendResult result = pushChannel.send(request);
        
        // Verificar resultado
        System.out.println();
        System.out.println("RESULTADO:");
        System.out.println("  Éxito: " + result.isSuccess());
        System.out.println("  Message ID: " + result.getMessageId());
        System.out.println("  Mensaje: " + result.getMessage());
        System.out.println();
    }
    
    /**
     * Ejemplo de configuración global con múltiples canales.
     */
    private static void exampleGlobalConfiguration() {
        System.out.println("-".repeat(60));
        System.out.println("EJEMPLO 4: CONFIGURACIÓN GLOBAL");
        System.out.println("-".repeat(60));
        System.out.println();
        
        // Configuración global
        NotificationConfig globalConfig = NotificationConfig.builder()
            .globalTimeout(java.time.Duration.ofSeconds(30))
            .maxRetries(3)
            .retryStrategy(NotificationConfig.RetryStrategy.EXPONENTIAL)
            .retryDelay(java.time.Duration.ofSeconds(1))
            .enableLogging(true)
            .enableMetrics(true)
            .build();
        
        System.out.println("Configuración Global:");
        System.out.println("  Timeout: " + globalConfig.getGlobalTimeout());
        System.out.println("  Max Retries: " + globalConfig.getMaxRetries());
        System.out.println("  Retry Strategy: " + globalConfig.getRetryStrategy());
        System.out.println("  Logging enabled: " + globalConfig.isEnableLogging());
        System.out.println("  Metrics enabled: " + globalConfig.isEnableMetrics());
        System.out.println();
        
        // Mostrar que todos los canales están disponibles
        EmailChannelConfig emailConfig = EmailChannelConfig.builder()
            .host("smtp.example.com")
            .fromAddress("test@example.com")
            .build();
        NotificationChannel emailChannel = new EmailChannel(emailConfig);
        
        SmsChannelConfig smsConfig = SmsChannelConfig.builder()
            .fromNumber("+1234567890")
            .build();
        NotificationChannel smsChannel = new SmsChannel(smsConfig);
        
        PushChannelConfig pushConfig = PushChannelConfig.builder()
            .projectId("test-project")
            .build();
        NotificationChannel pushChannel = new PushChannel(pushConfig);
        
        System.out.println("Disponibilidad de Canales:");
        System.out.println("  Email: " + emailChannel.isAvailable() + " (" + emailChannel.getChannelName() + ")");
        System.out.println("  SMS: " + smsChannel.isAvailable() + " (" + smsChannel.getChannelName() + ")");
        System.out.println("  Push: " + pushChannel.isAvailable() + " (" + pushChannel.getChannelName() + ")");
        System.out.println();
        
        // Mostrar soporte de solicitudes
        NotificationRequest emailRequest = NotificationRequest.builder()
            .content(NotificationContent.plainText("Test"))
            .recipients(List.of(Recipient.of("test@email.com")))
            .build();
        
        NotificationRequest smsRequest = NotificationRequest.builder()
            .content(NotificationContent.plainText("Test"))
            .recipients(List.of(Recipient.of("+1234567890")))
            .build();
        
        NotificationRequest pushRequest = NotificationRequest.builder()
            .content(NotificationContent.plainText("Test"))
            .recipients(List.of(Recipient.builder()
                    .identifier("user")
                    .type(RecipientType.TO)
                    .metadata(Map.of("deviceToken", "token123"))
                    .build()))
            .build();
        
        System.out.println("Compatibilidad de Solicitudes:");
        System.out.println("  EmailRequest soportado por EmailChannel: " + emailChannel.supports(emailRequest));
        System.out.println("  SmsRequest soportado por SmsChannel: " + smsChannel.supports(smsRequest));
        System.out.println("  PushRequest soportado por PushChannel: " + pushChannel.supports(pushRequest));
        System.out.println();
    }
}
