/**
 * Controller responsável por gerenciar o fluxo de mensagens via WebSocket.
 * Autor: Leonardo Gambaroni Alves
 * RA: 25003494
 * Intercepta as mensagens enviadas pelos clientes e realiza o broadcast para o tópico.
 */

package com.alugamaquina.backend.controller;

import com.alugamaquina.backend.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    // Recebe mensagens enviadas para o destino "/app/chat.enviar"  e as reencaminha para todos os inscritos em "/topic/mensagens"
    @MessageMapping("/chat.enviar")
    @SendTo("/topic/mensagens")
    public ChatMessage enviarMensagem(ChatMessage mensagem) {
        return mensagem;
    }
}