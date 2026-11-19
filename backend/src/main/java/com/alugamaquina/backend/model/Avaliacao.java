/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Modelo de entidade Avaliacao pos-locacao de equipamentos e locadores
*/

package com.alugamaquina.backend.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidade que armazena as avaliacoes pos-locacao de equipamentos e atendimento.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
@Document(collection = "avaliacoes")
public class Avaliacao implements Comparable<Avaliacao>, Cloneable {

    @Id
    private String id;

    // Ids que ligam a avaliacao a locacao, as pessoas e a maquina
    @Indexed(unique = true)
    private String locacaoId;
    @Indexed
    private String locadorId;
    private String locatarioId;
    @Indexed
    private String maquinaId;

    // Cinco notas, cada uma de 1 a 5
    private int notaDesempenho;   // 1 a 5
    private int notaConservacao;  // 1 a 5
    private int notaFidelidade;   // 1 a 5
    private int notaPontualidade; // 1 a 5
    private int notaSuporte;      // 1 a 5

    // Media das 5 notas e dados extras
    private double mediaFinal;
    private String comentario;
    private boolean recomenda;
    private LocalDateTime dataAvaliacao;

    // Construtor vazio: o Spring Data usa para ler do banco
    public Avaliacao() {
        this.dataAvaliacao = LocalDateTime.now();
    }

    // Construtor com todos os campos
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

    // ---- Metodos canonicos (padrao da disciplina) ----

    public Avaliacao(Avaliacao modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.locacaoId = modelo.locacaoId;
        this.locadorId = modelo.locadorId;
        this.locatarioId = modelo.locatarioId;
        this.maquinaId = modelo.maquinaId;
        this.notaDesempenho = modelo.notaDesempenho;
        this.notaConservacao = modelo.notaConservacao;
        this.notaFidelidade = modelo.notaFidelidade;
        this.notaPontualidade = modelo.notaPontualidade;
        this.notaSuporte = modelo.notaSuporte;
        this.mediaFinal = modelo.mediaFinal;
        this.comentario = modelo.comentario;
        this.recomenda = modelo.recomenda;
        this.dataAvaliacao = modelo.dataAvaliacao; // LocalDateTime e imutavel
    }

    @Override
    public Object clone() {
        Avaliacao retorno = null;

        try {
            retorno = new Avaliacao(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return "Avaliacao da locacao " + this.locacaoId
                + " - media " + this.mediaFinal
                + " - " + (this.recomenda ? "recomenda" : "nao recomenda");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        Avaliacao ava = (Avaliacao) obj;

        if (!Objects.equals(this.id, ava.id)) return false;
        if (!Objects.equals(this.locacaoId, ava.locacaoId)) return false;
        if (!Objects.equals(this.locadorId, ava.locadorId)) return false;
        if (!Objects.equals(this.locatarioId, ava.locatarioId)) return false;
        if (!Objects.equals(this.maquinaId, ava.maquinaId)) return false;
        if (this.notaDesempenho != ava.notaDesempenho) return false;
        if (this.notaConservacao != ava.notaConservacao) return false;
        if (this.notaFidelidade != ava.notaFidelidade) return false;
        if (this.notaPontualidade != ava.notaPontualidade) return false;
        if (this.notaSuporte != ava.notaSuporte) return false;
        if (Double.compare(this.mediaFinal, ava.mediaFinal) != 0) return false;
        if (!Objects.equals(this.comentario, ava.comentario)) return false;
        if (this.recomenda != ava.recomenda) return false;
        if (!Objects.equals(this.dataAvaliacao, ava.dataAvaliacao)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.locacaoId == null ? 0 : this.locacaoId.hashCode());
        retorno = retorno * 2 + (this.locadorId == null ? 0 : this.locadorId.hashCode());
        retorno = retorno * 2 + (this.locatarioId == null ? 0 : this.locatarioId.hashCode());
        retorno = retorno * 2 + (this.maquinaId == null ? 0 : this.maquinaId.hashCode());
        retorno = retorno * 2 + ((Integer) this.notaDesempenho).hashCode();
        retorno = retorno * 2 + ((Integer) this.notaConservacao).hashCode();
        retorno = retorno * 2 + ((Integer) this.notaFidelidade).hashCode();
        retorno = retorno * 2 + ((Integer) this.notaPontualidade).hashCode();
        retorno = retorno * 2 + ((Integer) this.notaSuporte).hashCode();
        retorno = retorno * 2 + ((Double) this.mediaFinal).hashCode();
        retorno = retorno * 2 + (this.comentario == null ? 0 : this.comentario.hashCode());
        retorno = retorno * 2 + ((Boolean) this.recomenda).hashCode();
        retorno = retorno * 2 + (this.dataAvaliacao == null ? 0 : this.dataAvaliacao.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    @Override
    public int compareTo(Avaliacao ava) {
        if (this.mediaFinal < ava.mediaFinal) return -666;
        if (this.mediaFinal > ava.mediaFinal) return 666;
        return 0;
    }
}