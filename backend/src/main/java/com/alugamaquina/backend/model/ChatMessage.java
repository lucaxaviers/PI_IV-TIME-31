/**
 * Classe modelo (DTO) que representa a estrutura de uma mensagem de chat.
 * Autor: Leonardo Gambaroni Alves
 * RA: 25003494
 * Define os atributos transmitidos nas conversas (remetente, conteúdo e horário).
 */

package com.alugamaquina.backend.model;

public class ChatMessage {
    private String remetente;
    private String conteudo;
    private String horario;

    // Construtor padrão vazio necessário para o mapeamento JSON
    public ChatMessage() {}

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}