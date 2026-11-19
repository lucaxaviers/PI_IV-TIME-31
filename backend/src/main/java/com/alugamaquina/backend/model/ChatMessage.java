/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Chat em Tempo Real e Mensageria
 *
 * Autor: Leonardo Gambaroni Alves | RA: 25003494
 * Descrição: Modelo de mensagem do chat (collection "chat_mensagens") com os metodos canonicos.
 */
package com.alugamaquina.backend.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_mensagens")
@CompoundIndex(name = "idx_chat_conversa_timestamp", def = "{'remetente': 1, 'destinatario': 1, 'timestamp': 1}")
public class ChatMessage implements Comparable<ChatMessage>, Cloneable {

    @Id
    private String id;
    private String remetente;
    private String destinatario;
    private String conteudo;
    private String horario;
    @Indexed
    private LocalDateTime timestamp;

    // Construtor vazio: o Spring Data usa para ler do banco
    public ChatMessage() {
    }

    // Construtor com os campos principais da mensagem
    public ChatMessage(String remetente, String destinatario, String conteudo, String horario) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.conteudo = conteudo;
        this.horario = horario;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemetente() {
        return this.remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getDestinatario() {
        return this.destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getConteudo() {
        return this.conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getHorario() {
        return this.horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ---- Metodos canonicos ----

    public ChatMessage(ChatMessage modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.remetente = modelo.remetente;
        this.destinatario = modelo.destinatario;
        this.conteudo = modelo.conteudo;
        this.horario = modelo.horario;
        this.timestamp = modelo.timestamp; // LocalDateTime e imutavel
    }

    @Override
    public Object clone() {
        ChatMessage retorno = null;

        try {
            retorno = new ChatMessage(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return "ChatMessage{id='" + this.id + "', remetente='" + this.remetente + "', destinatario='"
                + this.destinatario + "', horario='" + this.horario + "', timestamp=" + this.timestamp + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        ChatMessage msg = (ChatMessage) obj;

        if (!Objects.equals(this.id, msg.id)) return false;
        if (!Objects.equals(this.remetente, msg.remetente)) return false;
        if (!Objects.equals(this.destinatario, msg.destinatario)) return false;
        if (!Objects.equals(this.conteudo, msg.conteudo)) return false;
        if (!Objects.equals(this.horario, msg.horario)) return false;
        if (!Objects.equals(this.timestamp, msg.timestamp)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.remetente == null ? 0 : this.remetente.hashCode());
        retorno = retorno * 2 + (this.destinatario == null ? 0 : this.destinatario.hashCode());
        retorno = retorno * 2 + (this.conteudo == null ? 0 : this.conteudo.hashCode());
        retorno = retorno * 2 + (this.horario == null ? 0 : this.horario.hashCode());
        retorno = retorno * 2 + (this.timestamp == null ? 0 : this.timestamp.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    @Override
    public int compareTo(ChatMessage msg) {
        int porTimestamp = compararData(this.timestamp, msg.timestamp);
        if (porTimestamp != 0) return porTimestamp;

        return compararTexto(this.id, msg.id);
    }

    private static int compararData(LocalDateTime a, LocalDateTime b) {
        if (a == null && b == null) return 0;
        if (a == null) return -666;
        if (b == null) return 666;

        if (a.isBefore(b)) return -666;
        if (a.isAfter(b)) return 666;
        return 0;
    }

    private static int compararTexto(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -666;
        if (b == null) return 666;

        int c = a.compareTo(b);
        if (c < 0) return -666;
        if (c > 0) return 666;
        return 0;
    }
}
