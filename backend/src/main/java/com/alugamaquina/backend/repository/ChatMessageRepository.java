/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Chat em Tempo Real e Mensageria
 *
 * Autor: Leonardo Gambaroni Alves | RA: 25003494
 * Descrição: Repositório MongoDB para persistência e histórico de mensagens.
 */
package com.alugamaquina.backend.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.alugamaquina.backend.model.ChatMessage;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRemetenteAndDestinatarioOrderByTimestampAsc(String remetente, String destinatario);
    List<ChatMessage> findAllByOrderByTimestampAsc();
}