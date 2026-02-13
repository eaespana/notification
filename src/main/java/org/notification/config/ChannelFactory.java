package org.notification.config;

import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;
import org.notification.channel.email.EmailChannel;
import org.notification.channel.push.PushChannel;
import org.notification.channel.sms.SmsChannel;
import org.notification.config.channel.EmailChannelConfig;
import org.notification.config.channel.PushChannelConfig;
import org.notification.config.channel.SmsChannelConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Fábrica centralizada para la creación de instancias de canales de notificación.
 * 
 * Este componente implementa el patrón Factory y es responsable de:
 * - Registrar proveedores de canales
 * - Crear instancias de canales según su tipo
 * - Gestionar configuraciones específicas por canal
 * - Proveer acceso a configuraciones de manera centralizada
 * 
 * Patrones aplicados:
 * - Factory Method: Creación de instancias de canales
 * - Registry: Registro de proveedores disponibles
 * - Singleton: Una única instancia de fábrica por configuración
 * 
 * @author Notification Library
 * @version 1.0.0
 */
public final class ChannelFactory {
    
    private final Map<ChannelType, Supplier<NotificationChannel>> providers;
    private final Map<ChannelType, NotificationConfig.ChannelConfig> configurations;
    private final NotificationConfig globalConfig;
    
    /**
     * Constructor privado para forzar uso del builder.
     */
    private ChannelFactory(Builder builder) {
        this.providers = new HashMap<>(builder.providers);
        this.configurations = new HashMap<>(builder.configurations);
        this.globalConfig = builder.globalConfig;
        initializeDefaultProviders();
    }
    
    /**
     * Inicializa los proveedores por defecto si no están registrados.
     */
    private void initializeDefaultProviders() {
        // Email provider
        providers.putIfAbsent(ChannelType.EMAIL, () -> {
            EmailChannelConfig config = getConfigurationOrDefault(ChannelType.EMAIL);
            return config != null ? new EmailChannel(config) : new EmailChannel(createDefaultEmailConfig());
        });
        
        // SMS provider
        providers.putIfAbsent(ChannelType.SMS, () -> {
            SmsChannelConfig config = getConfigurationOrDefault(ChannelType.SMS);
            return config != null ? new SmsChannel(config) : new SmsChannel(createDefaultSmsConfig());
        });
        
        // Push provider
        providers.putIfAbsent(ChannelType.PUSH, () -> {
            PushChannelConfig config = getConfigurationOrDefault(ChannelType.PUSH);
            return config != null ? new PushChannel(config) : new PushChannel(createDefaultPushConfig());
        });
    }
    
    /**
     * Crea una configuración por defecto para Email.
     */
    private EmailChannelConfig createDefaultEmailConfig() {
        return EmailChannelConfig.builder()
            .host("localhost")
            .port(25)
            .fromAddress("noreply@default.local")
            .fromName("Notification Service")
            .build();
    }
    
    /**
     * Crea una configuración por defecto para SMS.
     */
    private SmsChannelConfig createDefaultSmsConfig() {
        return SmsChannelConfig.builder()
            .fromNumber("+0000000000")
            .build();
    }
    
    /**
     * Crea una configuración por defecto para Push.
     */
    private PushChannelConfig createDefaultPushConfig() {
        return PushChannelConfig.builder()
            .projectId("default-project")
            .build();
    }
    
    /**
     * Obtiene la configuración de un canal o null si no existe.
     */
    @SuppressWarnings("unchecked")
    private <T extends NotificationConfig.ChannelConfig> T getConfigurationOrDefault(ChannelType type) {
        return configurations.containsKey(type) 
            ? (T) configurations.get(type) 
            : null;
    }
    
    /**
     * Crea una instancia de canal para el tipo especificado.
     *
     * @param channelType el tipo de canal a crear
     * @return una Optional conteniendo el canal o vacío si no está registrado
     */
    public Optional<NotificationChannel> createChannel(ChannelType channelType) {
        Supplier<NotificationChannel> provider = providers.get(channelType);
        if (provider != null) {
            return Optional.of(provider.get());
        }
        return Optional.empty();
    }
    
    /**
     * Crea una instancia de canal para el tipo especificado.
     *
     * @param channelType el tipo de canal a crear
     * @param config      la configuración específica del canal
     * @return una Optional conteniendo el canal o vacío si no está registrado
     */
    public Optional<NotificationChannel> createChannel(ChannelType channelType, NotificationConfig.ChannelConfig config) {
        Supplier<NotificationChannel> provider = providers.get(channelType);
        if (provider != null) {
            // Crear canal con la configuración proporcionada
            return Optional.of(createChannelWithConfig(channelType, config));
        }
        return Optional.empty();
    }
    
    /**
     * Crea un canal con una configuración específica.
     */
    private NotificationChannel createChannelWithConfig(ChannelType type, NotificationConfig.ChannelConfig config) {
        return switch (type) {
            case EMAIL -> new EmailChannel((EmailChannelConfig) config);
            case SMS -> new SmsChannel((SmsChannelConfig) config);
            case PUSH -> new PushChannel((PushChannelConfig) config);
            default -> throw new IllegalArgumentException("Unknown channel type: " + type);
        };
    }
    
    /**
     * Obtiene la configuración de un canal específico.
     *
     * @param channelType el tipo de canal
     * @return Optional con la configuración o vacío
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationConfig.ChannelConfig> Optional<T> getConfiguration(ChannelType channelType) {
        return configurations.containsKey(channelType) 
            ? Optional.of((T) configurations.get(channelType)) 
            : Optional.empty();
    }
    
    /**
     * Verifica si un tipo de canal está soportado.
     *
     * @param channelType el tipo de canal
     * @return true si el canal está registrado
     */
    public boolean isSupported(ChannelType channelType) {
        return providers.containsKey(channelType);
    }
    
    /**
     * Obtiene todos los tipos de canales soportados.
     *
     * @return conjunto de tipos de canales disponibles
     */
    public java.util.Set<ChannelType> getSupportedChannels() {
        return providers.keySet();
    }
    
    /**
     * Obtiene la configuración global.
     *
     * @return la configuración global
     */
    public NotificationConfig getGlobalConfig() {
        return globalConfig;
    }
    
    /**
     * Builder para ChannelFactory.
     */
    public static final class Builder {
        
        private final Map<ChannelType, Supplier<NotificationChannel>> providers = new HashMap<>();
        private final Map<ChannelType, NotificationConfig.ChannelConfig> configurations = new HashMap<>();
        private NotificationConfig globalConfig = NotificationConfig.builder().build();
        
        /**
         * Establece la configuración global.
         *
         * @param config la configuración global
         * @return el builder
         */
        public Builder globalConfig(NotificationConfig config) {
            this.globalConfig = config;
            return this;
        }
        
        /**
         * Registra un proveedor de canal.
         *
         * @param type     el tipo de canal
         * @param provider el supplier que crea el canal
         * @return el builder
         */
        public Builder registerProvider(ChannelType type, Supplier<NotificationChannel> provider) {
            this.providers.put(type, provider);
            return this;
        }
        
        /**
         * Registra un proveedor de canal con su configuración.
         *
         * @param type     el tipo de canal
         * @param provider el supplier que crea el canal
         * @param config   la configuración del canal
         * @return el builder
         */
        public Builder registerProvider(
                ChannelType type, 
                Supplier<NotificationChannel> provider,
                NotificationConfig.ChannelConfig config) {
            this.providers.put(type, provider);
            this.configurations.put(type, config);
            return this;
        }
        
        /**
         * Registra una configuración de canal.
         *
         * @param config la configuración del canal
         * @return el builder
         */
        public Builder registerConfiguration(NotificationConfig.ChannelConfig config) {
            this.configurations.put(config.getChannelType(), config);
            return this;
        }
        
        /**
         * Construye la instancia de ChannelFactory.
         *
         * @return una nueva instancia de ChannelFactory
         */
        public ChannelFactory build() {
            return new ChannelFactory(this);
        }
    }
    
    /**
     * Crea un nuevo builder.
     *
     * @return un nuevo builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
