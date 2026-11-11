package com.alugamaquina.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidade que armazena as avaliacoes pos-locacao de equipamentos e atendimento.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
@Document(collection = "avaliacoes")
public class Avaliacao {

    @Id
    private String id;

    private String locacaoId;
    private String locadorId;
    private String locatarioId;
    private String maquinaId;

    private int notaDesempenho;   // 1 a 5
    private int notaConservacao;  // 1 a 5
    private int notaFidelidade;   // 1 a 5
    private int notaPontualidade; // 1 a 5
    private int notaSuporte;      // 1 a 5

    private double mediaFinal;
    private String comentario;
    private boolean recomenda;
    private LocalDateTime dataAvaliacao;

    public Avaliacao() {
        this.dataAvaliacao = LocalDateTime.now();
    }

    public Avaliacao(String locacaoId, String locadorId, String locatarioId, String maquinaId,
                     int notaDesempenho, int notaConservacao, int notaFidelidade,
                     int notaPontualidade, int notaSuporte, double mediaFinal,
                     String comentario, boolean recomenda) {
        this.locacaoId = locacaoId;
        this.locadorId = locadorId;
        this.locatarioId = locatarioId;
        this.maquinaId = maquinaId;
        this.notaDesempenho = notaDesempenho;
        this.notaConservacao = notaConservacao;
        this.notaFidelidade = notaFidelidade;
        this.notaPontualidade = notaPontualidade;
        this.notaSuporte = notaSuporte;
        this.mediaFinal = mediaFinal;
        this.comentario = comentario;
        this.recomenda = recomenda;
        this.dataAvaliacao = LocalDateTime.now();
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

    public String getLocatarioId() {
        return locatarioId;
    }

    public void setLocatarioId(String locatarioId) {
        this.locatarioId = locatarioId;
    }

    public String getMaquinaId() {
        return maquinaId;
    }

    public void setMaquinaId(String maquinaId) {
        this.maquinaId = maquinaId;
    }

    public int getNotaDesempenho() {
        return notaDesempenho;
    }

    public void setNotaDesempenho(int notaDesempenho) {
        this.notaDesempenho = notaDesempenho;
    }

    public int getNotaConservacao() {
        return notaConservacao;
    }

    public void setNotaConservacao(int notaConservacao) {
        this.notaConservacao = notaConservacao;
    }

    public int getNotaFidelidade() {
        return notaFidelidade;
    }

    public void setNotaFidelidade(int notaFidelidade) {
        this.notaFidelidade = notaFidelidade;
    }

    public int getNotaPontualidade() {
        return notaPontualidade;
    }

    public void setNotaPontualidade(int notaPontualidade) {
        this.notaPontualidade = notaPontualidade;
    }

    public int getNotaSuporte() {
        return notaSuporte;
    }

    public void setNotaSuporte(int notaSuporte) {
        this.notaSuporte = notaSuporte;
    }

    public double getMediaFinal() {
        return mediaFinal;
    }

    public void setMediaFinal(double mediaFinal) {
        this.mediaFinal = mediaFinal;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isRecomenda() {
        return recomenda;
    }

    public void setRecomenda(boolean recomenda) {
        this.recomenda = recomenda;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}
