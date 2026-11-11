/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: DTO com as notas e comentario da avaliacao pos-locacao
*/

package com.alugamaquina.backend.dto;

/**
 * DTO com os dados enviados pelo front-end para cadastrar uma avaliacao pos-locacao.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
public class AvaliacaoRequest {

    private String locacaoId;
    private String locadorId;
    private String locatarioId;
    private String maquinaId;

    private int notaDesempenho;   // 1 a 5
    private int notaConservacao;  // 1 a 5
    private int notaFidelidade;   // 1 a 5
    private int notaPontualidade; // 1 a 5
    private int notaSuporte;      // 1 a 5

    private String comentario;
    private boolean recomenda;

    public AvaliacaoRequest() {
    }

    public AvaliacaoRequest(String locacaoId, String locadorId, String locatarioId, String maquinaId,
                            int notaDesempenho, int notaConservacao, int notaFidelidade,
                            int notaPontualidade, int notaSuporte, String comentario, boolean recomenda) {
        this.locacaoId = locacaoId;
        this.locadorId = locadorId;
        this.locatarioId = locatarioId;
        this.maquinaId = maquinaId;
        this.notaDesempenho = notaDesempenho;
        this.notaConservacao = notaConservacao;
        this.notaFidelidade = notaFidelidade;
        this.notaPontualidade = notaPontualidade;
        this.notaSuporte = notaSuporte;
        this.comentario = comentario;
        this.recomenda = recomenda;
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
}
