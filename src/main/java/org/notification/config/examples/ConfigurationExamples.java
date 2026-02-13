package org.notification.config.examples;

import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.channel.email.EmailChannel;
import org.notification.channel.push.PushChannel;
import org.notification.channel.sms.SmsChannel;
import org.notification.config.ChannelFactory;
import org.notification.config.NotificationConfig;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.config.channel.PushChannelConfig;
import org.notification.config.channel.SmsChannelConfig;
import org.notification.model.*;
import org.notification.model.NotificationRequest.Priority;

import java.util.List;
import java.util.Optional;

import java.time.Duration;
import java.util.Optional;

/**
 * Ejemplos de configuración de la librería de notificaciones.
 * 
 * Este archivo demuestra las diferentes formas de configurar
 * el sistema de notificaciones usando Builder y Factory patterns.
 * 
 * @author Notification Library
 */
public class ConfigurationExamples {
    
    /**
     * Ejemplo 1: Configuración básica con un solo canal.
     */
    public static void basicConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 1: CONFIGURACIÓN BÁSICA");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Configuración global básica
        NotificationConfig config = NotificationConfig.builder()
            .globalTimeout(Duration.ofSeconds(30))
            .maxRetries(3)
            .enableLogging(true)
            .build();
        
        // Crear fábrica y registrar canal
        ChannelFactory factory = ChannelFactory.builder()
            .globalConfig(config)
            .registerProvider(ChannelType.EMAIL, () -> new EmailChannel(
                EmailChannelConfig.builder()
                    .host("smtp.gmail.com")
                    .port(587)
                    .fromAddress("noreply@miapp.com")
                    .build()
            ))
            .build();
        
        // Usar la fábrica para crear el canal
        Optional<NotificationChannel> channel = factory.createChannel(ChannelType.EMAIL);
        
        channel.ifPresent(c -> {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.withSubject("Hola", "Mensaje de prueba"))
                .recipients(List.of(Recipient.of("usuario@email.com")))
                .build();
            
            SendResult result = c.send(request);
            System.out.println("Éxito: " + result.isSuccess());
            System.out.println("Message ID: " + result.getMessageId());
        });
        
        System.out.println();
    }
    
    /**
     * Ejemplo 2: Configuración con múltiples proveedores.
     */
    public static void multiProviderConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 2: MÚLTIPLES PROVEEDORES");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Configuración global
        NotificationConfig globalConfig = NotificationConfig.builder()
            .globalTimeout(Duration.ofSeconds(30))
            .maxRetries(3)
            .retryStrategy(NotificationConfig.RetryStrategy.EXPONENTIAL)
            .retryDelay(Duration.ofSeconds(1))
            .build();
        
        // Configuraciones específicas de cada proveedor
        EmailChannelConfig sendgridConfig = EmailChannelConfig.builder()
            .host("smtp.sendgrid.net")
            .port(587)
            .fromAddress("noreply@miapp.com")
            .build();
        
        EmailChannelConfig mailgunConfig = EmailChannelConfig.builder()
            .host("smtp.mailgun.org")
            .port(587)
            .fromAddress("notifications@miapp.com")
            .build();
        
        // Crear fábrica con múltiples proveedores del mismo tipo
        ChannelFactory factory = ChannelFactory.builder()
            .globalConfig(globalConfig)
            // Proveedor principal
            .registerProvider(ChannelType.EMAIL, () -> new EmailChannel(sendgridConfig), sendgridConfig)
            // Registro directo de configuración (útil para switch de proveedores)
            .registerConfiguration(mailgunConfig)
            .build();
        
        // Canal primario
        Optional<NotificationChannel> primaryChannel = factory.createChannel(ChannelType.EMAIL);
        primaryChannel.ifPresent(c -> 
            System.out.println("Proveedor activo: " + c.getChannelName())
        );
        
        // Obtener configuración alternativa
        factory.getConfiguration(ChannelType.EMAIL)
            .filter(config -> config instanceof EmailChannelConfig)
            .map(config -> (EmailChannelConfig) config)
            .ifPresent(config -> {
                System.out.println("Proveedor alternativo disponible: " + config.getHost());
            });
        
        System.out.println();
    }
    
    /**
     * Ejemplo 3: Configuración completa con todos los canales.
     */
    public static void fullConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 3: CONFIGURACIÓN COMPLETA");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Configuración global
        NotificationConfig globalConfig = NotificationConfig.builder()
            .globalTimeout(Duration.ofSeconds(30))
            .maxRetries(3)
            .retryStrategy(NotificationConfig.RetryStrategy.EXPONENTIAL)
            .retryDelay(Duration.ofSeconds(2))
            .maxRetryDelay(Duration.ofSeconds(60))
            .enableLogging(true)
            .enableMetrics(true)
            .enableValidation(true)
            .operationMode(NotificationConfig.OperationMode.SYNC)
            .build();
        
        // Configuración de Email - SendGrid
        EmailChannelConfig emailConfig = EmailChannelConfig.builder()
            .host("smtp.sendgrid.net")
            .port(587)
            .username("apikey")
            .password("SG.xxxxxxxx")
            .fromAddress("noreply@miapp.com")
            .fromName("Mi App")
            .useTls(true)
            .connectionTimeoutMs(5000)
            .readTimeoutMs(10000)
            .build();
        
        // Configuración de SMS - Twilio
        SmsChannelConfig smsConfig = SmsChannelConfig.builder()
            .accountSid("ACxxxxxxxx")
            .authToken("xxxxxxxx")
            .fromNumber("+1234567890")
            .provider(SmsChannelConfig.SmsProvider.TWILIO)
            .maxCharactersPerSms(160)
            .build();
        
        // Configuración de Push - Firebase
        PushChannelConfig pushConfig = PushChannelConfig.builder()
            .projectId("mi-proyecto-firebase")
            .clientEmail("firebase-adminsdk@mi-proyecto.iam.gserviceaccount.com")
            .privateKey("-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----")
            .provider(PushChannelConfig.PushProvider.FIREBASE)
            .defaultAndroidChannelId("default")
            .defaultIcon("ic_notification")
            .build();
        
        // Crear fábrica con todos los canales
        ChannelFactory factory = ChannelFactory.builder()
            .globalConfig(globalConfig)
            .registerProvider(ChannelType.EMAIL, () -> new EmailChannel(emailConfig), emailConfig)
            .registerProvider(ChannelType.SMS, () -> new SmsChannel(smsConfig), smsConfig)
            .registerProvider(ChannelType.PUSH, () -> new PushChannel(pushConfig), pushConfig)
            .build();
        
        // Mostrar canales disponibles
        System.out.println("Canales registrados:");
        factory.getSupportedChannels().forEach(type -> 
            System.out.println("  - " + type + ": " + (factory.isSupported(type) ? "✓" : "✗"))
        );
        
        System.out.println();
    }
    
    /**
     * Ejemplo 4: Configuración con proveedor personalizado.
     */
    public static void customProviderConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 4: PROVEEDOR PERSONALIZADO");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Crear un canal personalizado (implementando NotificationChannel)
        NotificationChannel customEmailChannel = new NotificationChannel() {
            @Override
            public SendResult send(NotificationRequest request) {
                System.out.println(">>> ENVIANDO VÍA PROVEEDOR PERSONALIZADO <<<");
                System.out.println("Email: " + request.getRecipients().get(0).getIdentifier());
                System.out.println("Asunto: " + request.getContent().getSubject());
                return SendResult.success("CUSTOM-" + System.currentTimeMillis());
            }
            
            @Override
            public ChannelType getChannelType() {
                return ChannelType.EMAIL;
            }
        };
        
        // Registrar el proveedor personalizado
        ChannelFactory factory = ChannelFactory.builder()
            .registerProvider(ChannelType.EMAIL, () -> customEmailChannel)
            .build();
        
        // Usar el canal personalizado
        factory.createChannel(ChannelType.EMAIL).ifPresent(channel -> {
            NotificationRequest request = NotificationRequest.builder()
                .content(NotificationContent.withSubject("Custom", "Mensaje customizado"))
                .recipients(List.of(Recipient.of("custom@email.com")))
                .build();
            
            channel.send(request);
        });
        
        System.out.println();
    }
    
    /**
     * Ejemplo 5: Cambio dinámico de proveedor.
     */
    public static void dynamicProviderSwitch() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 5: CAMBIO DINÁMICO DE PROVEEDOR");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Configuraciones de diferentes proveedores
        EmailChannelConfig sendgrid = EmailChannelConfig.builder()
            .host("smtp.sendgrid.net")
            .fromAddress("sendgrid@miapp.com")
            .build();
        
        EmailChannelConfig mailgun = EmailChannelConfig.builder()
            .host("smtp.mailgun.org")
            .fromAddress("mailgun@miapp.com")
            .build();
        
        EmailChannelConfig awsSes = EmailChannelConfig.builder()
            .host("email-smtp.us-east-1.amazonaws.com")
            .fromAddress("aws@miapp.com")
            .build();
        
        // Factory con configuración inicial
        ChannelFactory factory = ChannelFactory.builder()
            .registerProvider(ChannelType.EMAIL, () -> new EmailChannel(sendgrid), sendgrid)
            .build();
        
        // Simular cambio de proveedor
        System.out.println("Proveedor actual: " + 
            factory.createChannel(ChannelType.EMAIL).map(NotificationChannel::getChannelName).orElse("N/A")
        );
        
        // Cambiar a Mailgun (regenerando factory con nueva configuración)
        factory = ChannelFactory.builder()
            .registerProvider(ChannelType.EMAIL, () -> new EmailChannel(mailgun), mailgun)
            .build();
        
        System.out.println("Proveedor cambiado a: " + 
            factory.createChannel(ChannelType.EMAIL).map(NotificationChannel::getChannelName).orElse("N/A")
        );
        
        // Cambiar a AWS SES
        factory = ChannelFactory.builder()
            .registerProvider(ChannelType.EMAIL, () -> new EmailChannel(awsSes), awsSes)
            .build();
        
        System.out.println("Proveedor cambiado a: " + 
            factory.createChannel(ChannelType.EMAIL).map(NotificationChannel::getChannelName).orElse("N/A")
        );
        
        System.out.println();
    }
    
    /**
     * Ejemplo 6: Configuración mínima vs completa.
     */
    public static void minVsFullConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("EJEMPLO 6: MÍNIMA VS COMPLETA");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Configuración mínima (valores por defecto)
        NotificationConfig minimalConfig = NotificationConfig.builder().build();
        System.out.println("Configuración mínima:");
        System.out.println("  Timeout: " + minimalConfig.getGlobalTimeout());
        System.out.println("  Max Retries: " + minimalConfig.getMaxRetries());
        System.out.println("  Retry Strategy: " + minimalConfig.getRetryStrategy());
        System.out.println("  Logging: " + minimalConfig.isEnableLogging());
        System.out.println("  Metrics: " + minimalConfig.isEnableMetrics());
        System.out.println();
        
        // Configuración completa
        NotificationConfig fullConfig = NotificationConfig.builder()
            .globalTimeout(Duration.ofSeconds(60))
            .maxRetries(5)
            .retryStrategy(NotificationConfig.RetryStrategy.EXPONENTIAL)
            .retryDelay(Duration.ofSeconds(5))
            .maxRetryDelay(Duration.ofSeconds(300))
            .enableLogging(true)
            .enableMetrics(true)
            .enableValidation(true)
            .operationMode(NotificationConfig.OperationMode.ASYNC)
            .build();
        
        System.out.println("Configuración completa:");
        System.out.println("  Timeout: " + fullConfig.getGlobalTimeout());
        System.out.println("  Max Retries: " + fullConfig.getMaxRetries());
        System.out.println("  Retry Strategy: " + fullConfig.getRetryStrategy());
        System.out.println("  Logging: " + fullConfig.isEnableLogging());
        System.out.println("  Metrics: " + fullConfig.isEnableMetrics());
        System.out.println("  Validation: " + fullConfig.isEnableValidation());
        System.out.println("  Operation Mode: " + fullConfig.getOperationMode());
        System.out.println();
    }
    
    /**
     * Método principal para ejecutar todos los ejemplos.
     */
    public static void main(String[] args) {
        System.out.println("\n" + "#".repeat(60));
        System.out.println("# EJEMPLOS DE CONFIGURACIÓN DE LA LIBRERÍA");
        System.out.println("#".repeat(60) + "\n");
        
        basicConfiguration();
        multiProviderConfiguration();
        fullConfiguration();
        customProviderConfiguration();
        dynamicProviderSwitch();
        minVsFullConfiguration();
        
        System.out.println("#".repeat(60));
        System.out.println("# TODOS LOS EJEMPLOS COMPLETADOS");
        System.out.println("#".repeat(60));
    }
}
