# Notification Library

Una librería de notificaciones en Java agnóstica a frameworks para envío a través de múltiples canales (Email, SMS, Push Notification).

---

## Tabla de Contenidos

- [Características Principales](#características-principales)
- [Instalación](#instalación)
- [Docker](#docker)
- [Quick Start](#quick-start)
- [Configuración](#configuración)
- [Proveedores Soportados](#proveedores-soportados)
- [Referencia de API](#referencia-de-api)
- [Seguridad](#seguridad)
- [Arquitectura](#arquitectura)

---

## Características Principales

- **Agnóstica a frameworks**: Sin dependencias de Spring, Quarkus u otros frameworks
- **Configuración por código Java**: Toda la configuración se realiza mediante clases Java
- **Extensible**: Agrega nuevos canales sin modificar código existente
- **Multi-canal**: Email, SMS, Push Notification y más
- **Manejo de errores estructurado**: Excepciones tipadas y resultados detallados
- **100% Java**: Solo Java estándar y Lombok
- **Patrones de diseño**: Builder, Factory, Strategy, Template Method

---

## Instalación

### Maven

Agrega la siguiente dependencia a tu archivo `pom.xml`:

```xml
<dependency>
    <groupId>org.notification</groupId>
    <artifactId>notification</artifactId>
    <version>1.0.0</version>
</dependency>
```


### Requisitos

- Java 21 o superior
- Maven 3.6+

---

## Docker

La librería incluye un Dockerfile para facilitar la ejecución de ejemplos y pruebas sin necesidad de configurar Java localmente.

### Construir la imagen

```bash
docker build -t notification-lib .
```

### Ejecutar la imagen

```bash
docker run --rm notification-lib
```

### Ver los ejemplos en acción

El contenedor ejecutará la clase [`Main`](src/main/java/org/notification/Main.java) que contiene ejemplos de uso de todos los canales implementados (Email, SMS, Push).

---

## Quick Start

### Envío de un Email Simple

```java
import org.notification.*;
import org.notification.channel.email.EmailChannel;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.model.*;

import java.util.List;

// 1. Configurar el canal de Email
EmailChannelConfig config = EmailChannelConfig.builder()
    .host("smtp.sendgrid.net")
    .port(587)
    .username("apikey")
    .password("SG.xxxxxxxxxxxx")
    .fromAddress("noreply@miempresa.com")
    .fromName("Mi Empresa")
    .useTls(true)
    .build();

// 2. Crear el canal
NotificationChannel emailChannel = new EmailChannel(config);

// 3. Crear el contenido
NotificationContent content = NotificationContent.builder()
    .subject("Bienvenido a Mi Empresa")
    .body("Hola Juan, gracias por registrarte en nuestra plataforma.")
    .build();

// 4. Crear destinatario
Recipient recipient = Recipient.to("juan.perez@email.com");

// 5. Crear la solicitud
NotificationRequest request = NotificationRequest.builder()
    .content(content)
    .recipients(List.of(recipient))
    .priority(Priority.NORMAL)
    .correlationId("CORR-001")
    .build();

// 6. Enviar
SendResult result = emailChannel.send(request);

// 7. Verificar resultado
if (result.isSuccess()) {
    System.out.println("Email enviado: " + result.getMessageId());
} else {
    System.out.println("Error: " + result.getMessage());
}
```

---

## Configuración

### Configuración de Email

```java
EmailChannelConfig config = EmailChannelConfig.builder()
    .host("smtp.gmail.com")                    // Servidor SMTP (requerido)
    .port(587)                                 // Puerto (default: 587)
    .username("tu-email@gmail.com")            // Usuario (requerido)
    .password("tu-app-password")               // Contraseña o App Password (requerido)
    .fromAddress("noreply@miempresa.com")      // Remitente (requerido)
    .fromName("Mi Empresa")                    // Nombre del remitente (opcional)
    .useTls(true)                              // Usar TLS (default: true)
    .connectionTimeoutMs(5000)                 // Timeout de conexión (default: 5000ms)
    .readTimeoutMs(10000)                      // Timeout de lectura (default: 10000ms)
    .build();
```

### Configuración de SMS

```java
SmsChannelConfig config = SmsChannelConfig.builder()
    .accountSid("ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")  // Account SID (requerido)
    .authToken("tu-auth-token")                     // Token de autenticación (requerido)
    .fromNumber("+1234567890")                      // Número remitente (requerido)
    .apiUrl("https://api.twilio.com/2010-04-01")   // URL de la API (default: Twilio)
    .provider(SmsChannelConfig.SmsProvider.TWILIO) // Proveedor (default: TWILIO)
    .connectionTimeoutMs(5000)                      // Timeout (default: 5000ms)
    .readTimeoutMs(10000)                           // Timeout (default: 10000ms)
    .maxCharactersPerSms(160)                       // Max caracteres por SMS (default: 160)
    .build();
```

### Configuración de Push Notifications

```java
PushChannelConfig config = PushChannelConfig.builder()
    .projectId("mi-proyecto-firebase")                          // Project ID (requerido)
    .clientEmail("firebase-adminsdk@proyecto.iam.gserviceaccount.com")  // Email de servicio (requerido)
    .privateKey("-----BEGIN PRIVATE KEY-----\n...")             // Private Key (requerido)
    .apiUrl("https://fcm.googleapis.com/v1/projects/%s/messages:send") // URL API
    .provider(PushChannelConfig.PushProvider.FIREBASE)          // Proveedor (default: FIREBASE)
    .defaultAndroidChannelId("notificaciones_generales")        // Canal Android
    .defaultIcon("ic_notification")                             // Icono por defecto
    .defaultColor("#FF5722")                                    // Color (#RRGGBB)
    .connectionTimeoutMs(5000)                                   // Timeout (default: 5000ms)
    .readTimeoutMs(10000)                                        // Timeout (default: 10000ms)
    .build();
```

### Configuración Global

```java
NotificationConfig config = NotificationConfig.builder()
    .globalTimeout(Duration.ofSeconds(30))              // Timeout global (default: 30s)
    .maxRetries(3)                                      // Max reintentos (default: 3)
    .retryStrategy(RetryStrategy.EXPONENTIAL)           // Estrategia (EXPONENTIAL, FIXED, NONE)
    .retryDelay(Duration.ofSeconds(1))                  // Delay base (default: 1s)
    .maxRetryDelay(Duration.ofSeconds(60))             // Delay máximo (default: 60s)
    .enableLogging(true)                                // Habilitar logging (default: true)
    .enableMetrics(true)                                // Habilitar métricas (default: true)
    .enableValidation(true)                             // Habilitar validación (default: true)
    .operationMode(OperationMode.SYNC)                  // Modo (SYNC o ASYNC)
    .build();
```

---

## Proveedores Soportados

### Email

| Proveedor | Host SMTP | Puerto | Notas |
|-----------|-----------|--------|-------|
| Gmail | `smtp.gmail.com` | 587 | Requiere App Password |
| SendGrid | `smtp.sendgrid.net` | 587 | Username: `apikey` |
| Amazon SES | `email-smtp.us-east-1.amazonaws.com` | 587 | Requiere credenciales IAM |
| Outlook/Office365 | `smtp.office365.com` | 587 | |
| Yahoo | `smtp.mail.yahoo.com` | 587 | Requiere App Password |

### SMS

| Proveedor | Características | Región |
|-----------|-----------------|--------|
| Twilio | Líder del mercado, API simple | Global |
| AWS SNS | Integración con ecosistema AWS | Global |
| Nexmo (Vonage) | Buena cobertura internacional | Global |

### Push Notifications

| Proveedor | Plataformas | Notas |
|-----------|-------------|-------|
| Firebase (FCM) | Android, iOS, Web | Gratuito hasta cierto límite |
| Apple (APNs) | iOS, macOS | Requiere cuenta de desarrollador |
| Huawei Push Kit | Huawei devices | Para dispositivos Huawei |

---

## Referencia de API

### Clases Principales

#### NotificationService

Fachada principal para el envío de notificaciones.

```java
public class NotificationService {
    // Crear builder
    public static NotificationServiceBuilder builder()
    
    // Enviar notificación
    public SendResult send(NotificationRequest request)
}
```

#### NotificationChannel

Interfaz base para todos los canales.

```java
public interface NotificationChannel {
    // Enviar notificación
    SendResult send(NotificationRequest request);
    
    // Obtener tipo de canal
    ChannelType getChannelType();
    
    // Verificar soporte
    default boolean supports(NotificationRequest request)
    
    // Verificar disponibilidad
    default boolean isAvailable()
    
    // Obtener nombre
    default String getChannelName()
}
```

#### NotificationRequest

Representa una solicitud de notificación.

```java
public final class NotificationRequest {
    // Métodos estáticos de construcción
    public static NotificationRequestBuilder builder()
    
    // Agregar destinatario
    public NotificationRequest addRecipient(Recipient recipient)
    
    // Obtener destinatario principal
    public Recipient getPrimaryRecipient()
    
    // Verificar si hay destinatarios
    public boolean hasRecipients()
    
    // Obtener metadato por clave
    public Object getMetadata(String key)
}
```

#### NotificationContent

Representa el contenido de la notificación.

```java
public final class NotificationContent {
    // Métodos estáticos de construcción
    public static NotificationContent plainText(String body)
    public static NotificationContent withSubject(String subject, String body)
    
    // Clase anidada para adjuntos
    public static final class Attachment {
        public static Attachment fromBytes(String filename, String contentType, byte[] content)
        public static Attachment fromUrl(String filename, String contentType, String url)
    }
}
```

#### Recipient

Representa un destinatario.

```java
public class Recipient {
    // Métodos estáticos de construcción
    public static Recipient of(String identifier)
    public static Recipient to(String identifier)
    public static Recipient cc(String identifier)
    public static Recipient bcc(String identifier)
}
```

#### SendResult

Representa el resultado del envío.

```java
public final class SendResult {
    // Métodos estáticos de construcción
    public static SendResult success(String messageId)
    public static SendResult success(String messageId, String message)
    public static SendResult failure(String errorMessage)
    public static SendResult retry(String reason, Integer providerCode)
    
    // Verificar estado
    public boolean isSuccess()
    public boolean isFailure()
    public boolean shouldRetry()
    
    // Enumeración de estados
    public enum Status {
        SUCCESS,
        FAILURE,
        RETRY
    }
}
```

### Enumeraciones

#### ChannelType

```java
public enum ChannelType {
    EMAIL("email"),
    SMS("sms"),
    PUSH("push");
}
```

#### Priority

```java
public enum Priority {
    LOW,      // Prioridad baja - envío diferido
    NORMAL,   // Prioridad normal - envío estándar
    HIGH,     // Prioridad alta - envío acelerado
    URGENT    // Prioridad urgente - envío inmediato
}
```

#### RecipientType

```java
public enum RecipientType {
    TO,   // Destinatario principal
    CC,   // Copia
    BCC   // Copia oculta
}
```

---

## Seguridad

### Mejores Prácticas para Manejo de Credenciales

#### 1. Nunca expongas credenciales en código

❌ **Incorrecto**:
```java
EmailChannelConfig config = EmailChannelConfig.builder()
    .password("SG.my-secret-api-key")  // ¡NUNCA HAGAS ESTO!
    .build();
```

✅ **Correcto** - Usar variables de entorno:
```java
EmailChannelConfig config = EmailChannelConfig.builder()
    .password(System.getenv("EMAIL_API_KEY"))
    .build();
```

✅ **Correcto** - Usar archivos de configuración externos:
```java
// Cargar desde properties
Properties props = new Properties();
props.load(new FileInputStream("config/notification.properties"));

EmailChannelConfig config = EmailChannelConfig.builder()
    .password(props.getProperty("email.smtp.password"))
    .build();
```

#### 2. Usa credenciales específicas por ambiente

```java
// Desarrollo
EmailChannelConfig devConfig = EmailChannelConfig.builder()
    .host("smtp.test.com")
    .password(System.getenv("DEV_SMTP_PASSWORD"))
    .build();

// Producción
EmailChannelConfig prodConfig = EmailChannelConfig.builder()
    .host("smtp.production.com")
    .password(System.getenv("PROD_SMTP_PASSWORD"))
    .build();
```

#### 3. Rotación periódica de credenciales

- Cambia las API keys regularmente
- Usa credenciales de servicio con permisos mínimos
- Configura alerts para accesos sospechosos

#### 4. Valida las credenciales antes de usar

```java
public NotificationChannel createEmailChannel(EmailChannelConfig config) {
    // Validar que las credenciales no estén vacías
    if (config.getPassword() == null || config.getPassword().isEmpty()) {
        throw new IllegalArgumentException("Las credenciales no pueden estar vacías");
    }
    
    // Crear canal y verificar disponibilidad
    NotificationChannel channel = new EmailChannel(config);
    if (!channel.isAvailable()) {
        throw new ConfigurationException("El canal de email no está disponible");
    }
    
    return channel;
}
```

#### 5. Usa App Passwords en lugar de contraseñas reales

Para Gmail y otros proveedores que soportan 2FA:

1. Habilita 2FA en tu cuenta
2. Genera una App Password específica para tu aplicación
3. Usa la App Password en lugar de tu contraseña

#### 6. Limita los permisos de las credenciales

- Usa credenciales con solo los permisos necesarios
- Para Email: solo permisos de envío SMTP
- Para SMS: solo permisos de envío de mensajes
- Para Push: solo permisos de envío atopics específicos

---

## Arquitectura

```
┌─────────────────────────────────────┐
│         NotificationService         │  (Fachada)
├─────────────────────────────────────┤
│         ChannelFactory              │  (Fábrica)
├────────────┬────────────┬───────────┤
│   Email    │    SMS     │   Push    │  (Canales)
│  Channel   │  Channel   │  Channel  │
├────────────┴────────────┴───────────┤
│        NotificationRequest          │  (Modelos)
│        NotificationContent          │
│           Recipient                 │
│          SendResult                 │
├─────────────────────────────────────┤
│        NotificationConfig           │  (Configuración)
├─────────────────────────────────────┤
│            Exception                │  (Manejo de errores)
└─────────────────────────────────────┘
```

### Patrones Aplicados

- **Builder**: Construcción fluida de objetos complejos
- **Factory**: Creación de instancias de canales
- **Strategy**: Diferentes estrategias de reintentos
- **Template Method**: Lógica común en canales base
- **Value Object**: Inmutabilidad en modelos
- **Facade**: NotificationService como punto de entrada simple
