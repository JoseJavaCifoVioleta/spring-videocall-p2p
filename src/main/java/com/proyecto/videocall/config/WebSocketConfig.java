package com.proyecto.videocall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Este es el punto de encuentro al que se conectará el cliente desde JavaScript
        registry.addEndpoint("/ws-signaling")
                .setAllowedOriginPatterns("*") // Permite conexiones desde cualquier origen en desarrollo
                .addInterceptors(new IpHandshakeInterceptor())
                .withSockJS(); // Habilita SockJS como alternativa si el navegador no soporta WebSockets nativos
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para los mensajes que van desde el cliente HACIA el servidor (ej: /app/sala)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo del canal al que se SUSCRIBEN los clientes para RECIBIR mensajes (ej: /topic/sala)
        registry.enableSimpleBroker("/topic");
    }
}
