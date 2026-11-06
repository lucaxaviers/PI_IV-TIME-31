/**
 * Classe de configuração do WebSocket para o chat em tempo real.
 * Autor: Leonardo Gambaroni Alves
 * RA: 25003494
 * Configura o broker de mensagens STOMP e o endpoint /ws-chat com suporte ao SockJS.
 */

package com.alugamaquina.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilita um broker simples de mensagens para o tópico de destino
        registry.enableSimpleBroker("/topic");
        // Define o prefixo para as rotas direcionadas às mensagens do aplicativo
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra o endpoint do WebSocket que o frontend vai utilizar para conectar
        registry.addEndpoint("/ws-chat").withSockJS();
    }
}