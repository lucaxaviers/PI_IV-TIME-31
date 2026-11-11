package com.alugamaquina.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO de resposta confirmando o registro da avaliacao e o recalculo da reputacao.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
public class AvaliacaoResponse {

    private boolean sucesso;
    private String mensagem;
    private String id;
    private String locacaoId;
    private String locadorId;
    private double mediaFinalAvaliacao;
    private double novaReputacaoLocador;
    private int totalAvaliacoesLocador;
    private LocalDateTime dataAvaliacao;

    public AvaliacaoResponse() {
    }

    public AvaliacaoResponse(boolean sucesso, String mensagem, String id, String locacaoId,
                             String locadorId, double mediaFinalAvaliacao,
                             double novaReputacaoLocador, int totalAvaliacoesLocador,
                             LocalDateTime dataAvaliacao) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.id = id;
        this.locacaoId = locacaoId;
        this.locadorId = locadorId;
        this.mediaFinalAvaliacao = mediaFinalAvaliacao;
        this.novaReputacaoLocador = novaReputacaoLocador;
        this.totalAvaliacoesLocador = totalAvaliacoesLocador;
        this.dataAvaliacao = dataAvaliacao;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocacaoId() {
        return locacaoId;
    }

    public void setLocacaoId(String locacaoId) {
        this.locacaoId = locacaoId;
    }

    public String getLocadorId() {
        return locadorId;
    }

    public void setLocadorId(String locadorId) {
        this.locadorId = locadorId;
    }

    public double getMediaFinalAvaliacao() {
        return mediaFinalAvaliacao;
    }

    public void setMediaFinalAvaliacao(double mediaFinalAvaliacao) {
        this.mediaFinalAvaliacao = mediaFinalAvaliacao;
    }

    public double getNovaReputacaoLocador() {
        return novaReputacaoLocador;
    }

    public void setNovaReputacaoLocador(double novaReputacaoLocador) {
        this.novaReputacaoLocador = novaReputacaoLocador;
    }

    public int getTotalAvaliacoesLocador() {
        return totalAvaliacoesLocador;
    }

    public void setTotalAvaliacoesLocador(int totalAvaliacoesLocador) {
        this.totalAvaliacoesLocador = totalAvaliacoesLocador;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}
