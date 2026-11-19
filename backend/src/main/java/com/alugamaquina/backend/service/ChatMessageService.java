/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Chat em Tempo Real e Mensageria
 *
 * Autor: Leonardo Gambaroni Alves | RA: 25003494
 * Descrição: Regras de negócio, sanitização e persistência de mensagens no MongoDB.
 */
package com.alugamaquina.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.alugamaquina.backend.model.ChatMessage;
import com.alugamaquina.backend.repository.ChatMessageRepository;

@Service
public class ChatMessageService {

    public static final int TAMANHO_MAXIMO = 1000;
    private final ChatMessageRepository repository;

    public ChatMessageService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    public ChatMessage salvarMensagem(ChatMessage mensagem) {
        if (mensagem == null) throw new IllegalArgumentException("A mensagem não pode ser nula.");
        if (mensagem.getRemetente() == null || mensagem.getRemetente().isBlank()) {
            throw new IllegalArgumentException("Remetente obrigatório.");
        }
        if (mensagem.getDestinatario() != null && mensagem.getRemetente().equalsIgnoreCase(mensagem.getDestinatario())) {
            throw new IllegalArgumentException("Remetente e destinatário não podem ser iguais.");
        }
        if (mensagem.getConteudo() == null || mensagem.getConteudo().isBlank()) {
            throw new IllegalArgumentException("O conteúdo da mensagem não pode estar vazio.");
        }
        if (mensagem.getConteudo().length() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A mensagem excede o limite de " + TAMANHO_MAXIMO + " caracteres.");
        }

        ChatMessage copia = (ChatMessage) mensagem.clone();
        copia.setConteudo(copia.getConteudo().replace("<", "&lt;").replace(">", "&gt;"));
        if (copia.getTimestamp() == null) copia.setTimestamp(LocalDateTime.now());
        return repository.save(copia);
    }

    public List<ChatMessage> listarHistorico(String remetente, String destinatario) {
        if (remetente != null && !remetente.isBlank() && destinatario != null && !destinatario.isBlank()) {
            return repository.findByRemetenteAndDestinatarioOrderByTimestampAsc(remetente, destinatario);
        }
        return repository.findAllByOrderByTimestampAsc();
    }
}