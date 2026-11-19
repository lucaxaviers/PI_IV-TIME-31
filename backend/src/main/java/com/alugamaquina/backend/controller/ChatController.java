/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Chat em Tempo Real e Mensageria
 *
 * Autor: Leonardo Gambaroni Alves | RA: 25003494
 * Descrição: Controller WebSocket e REST para envio e histórico de mensagens.
 */
package com.alugamaquina.backend.controller;

import java.util.List;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alugamaquina.backend.model.ChatMessage;
import com.alugamaquina.backend.service.ChatMessageService;

@RestController
public class ChatController {

    private final ChatMessageService chatService;

    public ChatController(ChatMessageService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.enviar")
    @SendTo("/topic/mensagens")
    public ChatMessage enviarMensagem(ChatMessage mensagem) {
        return chatService.salvarMensagem(mensagem);
    }

    @GetMapping("/api/chat/historico")
    public List<ChatMessage> historico(
            @RequestParam(required = false) String remetente,
            @RequestParam(required = false) String destinatario) {
        return chatService.listarHistorico(remetente, destinatario);
    }
}