/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Chat em Tempo Real e Mensageria
 *
 * Autor: Leonardo Gambaroni Alves | RA: 25003494
 * Descrição: Modelo de mensagem do chat (collection "chat_mensagens") com Sexteto Sagrado.
 */
package com.alugamaquina.backend.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_mensagens")
public class ChatMessage implements Comparable<ChatMessage>, Cloneable {

    private static final Comparator<ChatMessage> ORDEM_NATURAL = Comparator
            .comparing(ChatMessage::getTimestamp, Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder()))
            .thenComparing(ChatMessage::getId, Comparator.nullsFirst(Comparator.<String>naturalOrder()));

    @Id
    private String id;
    private String remetente;
    private String destinatario;
    private String conteudo;
    private String horario;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(ChatMessage modelo) throws Exception {
        if (modelo == null) throw new Exception("Modelo ausente");
        this.id = modelo.id;
        this.remetente = modelo.remetente;
        this.destinatario = modelo.destinatario;
        this.conteudo = modelo.conteudo;
        this.horario = modelo.horario;
        this.timestamp = modelo.timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRemetente() { return remetente; }
    public void setRemetente(String remetente) { this.remetente = remetente; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public Object clone() {
        ChatMessage ret = null;
        try { ret = new ChatMessage(this); } catch (Exception erro) {}
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        ChatMessage outro = (ChatMessage) obj;
        if (!Objects.equals(this.id, outro.id)) return false;
        if (!Objects.equals(this.remetente, outro.remetente)) return false;
        if (!Objects.equals(this.destinatario, outro.destinatario)) return false;
        if (!Objects.equals(this.conteudo, outro.conteudo)) return false;
        if (!Objects.equals(this.horario, outro.horario)) return false;
        if (!Objects.equals(this.timestamp, outro.timestamp)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int ret = 7;
        ret = 7 * ret + (this.id == null ? 0 : this.id.hashCode());
        ret = 7 * ret + (this.remetente == null ? 0 : this.remetente.hashCode());
        ret = 7 * ret + (this.destinatario == null ? 0 : this.destinatario.hashCode());
        ret = 7 * ret + (this.conteudo == null ? 0 : this.conteudo.hashCode());
        ret = 7 * ret + (this.horario == null ? 0 : this.horario.hashCode());
        ret = 7 * ret + (this.timestamp == null ? 0 : this.timestamp.hashCode());
        if (ret < 0) ret = -ret;
        return ret;
    }

    @Override
    public int compareTo(ChatMessage outra) { return ORDEM_NATURAL.compare(this, outra); }

    @Override
    public String toString() {
        return "ChatMessage{id='" + id + "', remetente='" + remetente + "', destinatario='" + destinatario
                + "', horario='" + horario + "', timestamp=" + timestamp + "}";
    }
}