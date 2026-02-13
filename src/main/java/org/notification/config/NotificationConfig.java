package org.notification.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.notification.channel.ChannelType;
import org.notification.channel.NotificationChannel;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Configuración global de la librería de notificaciones.
 * 
 * Esta clase centraliza toda la configuración necesaria para el funcionamiento
 * de la librería. Utiliza el patrón Builder para una construcción fluida y
 * el patrón Factory para el registro de canales.
 * 
 * Patrones aplicados:
 * - Builder: Construcción fluida de objetos complejos
 * - Factory: Creación de instancias de canales
 * - Singleton (config): Una única fuente de verdad para la configuración
 * 
 * @author Notification Library
 * @version 1.0.0
 */
@Getter
@ToString
@Builder
public final class NotificationConfig {
    
    /**
     * Timeout global para las operaciones de envío
     */
    @Builder.Default
    private final Duration globalTimeout = Duration.ofSeconds(30);
    
    /**
     * Número máximo de reintentos en caso de fallo
     */
    @Builder.Default
    private final int maxRetries = 3;
    
    /**
     * Estrategia de reintentos (fixed, exponential, none)
     */
    @Builder.Default
    private final RetryStrategy retryStrategy = RetryStrategy.EXPONENTIAL;
    
    /**
     * Delay base entre reintentos
     */
    @Builder.Default
    private final Duration retryDelay = Duration.ofSeconds(1);
    
    /**
     * Timeout máximo entre reintentos
     */
    @Builder.Default
    private final Duration maxRetryDelay = Duration.ofSeconds(60);
    
    /**
     * Proveedores de canales registrados
     * Maps ChannelType -> Supplier de NotificationChannel
     */
    private final Map<ChannelType, Supplier<NotificationChannel>> channelProviders;
    
    /**
     * Configuraciones específicas por canal
     */
    private final Map<ChannelType, ChannelConfig> channelConfigs;
    
    /**
     * Habilitar logging de operaciones
     */
    @Builder.Default
    private final boolean enableLogging = true;
    
    /**
     * Habilitar métricas
     */
    @Builder.Default
    private final boolean enableMetrics = true;
    
    /**
     * Habilitar validación de solicitudes
     */
    @Builder.Default
    private final boolean enableValidation = true;
    
    /**
     * Modo de operación (sync o async)
     */
    @Builder.Default
    private final OperationMode operationMode = OperationMode.SYNC;
    
    /**
     * Registro de canales por tipo.
     *
     * @param channelType el tipo de canal
     * @param provider    el supplier que crea instancias del canal
     * @return una nueva instancia con el canal registrado
     */
    public NotificationConfig registerChannel(
            ChannelType channelType, 
            Supplier<NotificationChannel> provider) {
        
        Map<ChannelType, Supplier<NotificationChannel>> updatedProviders = 
            new HashMap<>(getChannelProviders());
        updatedProviders.put(channelType, provider);
        
        return NotificationConfig.builder()
            .globalTimeout(this.globalTimeout)
            .maxRetries(this.maxRetries)
            .retryStrategy(this.retryStrategy)
            .retryDelay(this.retryDelay)
            .maxRetryDelay(this.maxRetryDelay)
            .channelProviders(updatedProviders)
            .channelConfigs(this.channelConfigs)
            .enableLogging(this.enableLogging)
            .enableMetrics(this.enableMetrics)
            .enableValidation(this.enableValidation)
            .operationMode(this.operationMode)
            .build();
    }
    
    /**
     * Registro de canales por tipo con configuración.
     *
     * @param channelType el tipo de canal
     * @param provider    el supplier que crea instancias del canal
     * @param config      la configuración específica del canal
     * @return una nueva instancia con el canal y configuración registrados
     */
    public NotificationConfig registerChannel(
            ChannelType channelType,
            Supplier<NotificationChannel> provider,
            ChannelConfig config) {
        
        Map<ChannelType, Supplier<NotificationChannel>> updatedProviders = 
            new HashMap<>(getChannelProviders());
        updatedProviders.put(channelType, provider);
        
        Map<ChannelType, ChannelConfig> updatedConfigs = 
            new HashMap<>(getChannelConfigs());
        updatedConfigs.put(channelType, config);
        
        return NotificationConfig.builder()
            .globalTimeout(this.globalTimeout)
            .maxRetries(this.maxRetries)
            .retryStrategy(this.retryStrategy)
            .retryDelay(this.retryDelay)
            .maxRetryDelay(this.maxRetryDelay)
            .channelProviders(updatedProviders)
            .channelConfigs(updatedConfigs)
            .enableLogging(this.enableLogging)
            .enableMetrics(this.enableMetrics)
            .enableValidation(this.enableValidation)
            .operationMode(this.operationMode)
            .build();
    }
    
    /**
     * Obtiene la configuración específica de un canal.
     *
     * @param channelType el tipo de canal
     * @return la configuración del canal o null si no existe
     */
    public ChannelConfig getChannelConfiguration(ChannelType channelType) {
        return channelConfigs != null ? channelConfigs.get(channelType) : null;
    }
    
    /**
     * Obtiene el supplier de un canal.
     *
     * @param channelType el tipo de canal
     * @return el supplier o null si no existe
     */
    public Supplier<NotificationChannel> getChannelProvider(ChannelType channelType) {
        return channelProviders != null ? channelProviders.get(channelType) : null;
    }
    
    /**
     * Verifica si un canal está registrado.
     *
     * @param channelType el tipo de canal
     * @return true si el canal está registrado
     */
    public boolean isChannelRegistered(ChannelType channelType) {
        return channelProviders != null && channelProviders.containsKey(channelType);
    }
    
    /**
     * Obtiene el mapa de proveedores (inicializado si es null).
     *
     * @return el mapa de proveedores
     */
    private Map<ChannelType, Supplier<NotificationChannel>> getChannelProviders() {
        return channelProviders != null ? channelProviders : new HashMap<>();
    }
    
    /**
     * Obtiene el mapa de configuraciones (inicializado si es null).
     *
     * @return el mapa de configuraciones
     */
    private Map<ChannelType, ChannelConfig> getChannelConfigs() {
        return channelConfigs != null ? channelConfigs : new HashMap<>();
    }
    
    /**
     * Estrategia de reintentos.
     */
    public enum RetryStrategy {
        /** No realizar reintentos */
        NONE,
        
        /** Delay fijo entre reintentos */
        FIXED,
        
        /** Delay exponencial con jitter */
        EXPONENTIAL
    }
    
    /**
     * Modo de operación del servicio.
     */
    public enum OperationMode {
        /** Operaciones síncronas (bloqueante) */
        SYNC,
        
        /** Operaciones asíncronas (no bloqueante) */
        ASYNC
    }
    
    /**
     * Interfaz base para configuraciones específicas de canal.
     */
    public interface ChannelConfig {
        ChannelType getChannelType();
    }
}
