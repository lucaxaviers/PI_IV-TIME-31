/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: DTO de resposta com os dados do contrato e o estado das assinaturas, para a tela preencher.
*/

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContratoResponse {

    private final boolean sucesso;
    private final String mensagem;
    private final String id;
    private final String locacaoId;
    private final String numero;
    private final String nomeLocador;
    private final String nomeLocatario;
    private final String descricaoMaquina;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final long diarias;
    private final BigDecimal valorTotal;
    private final BigDecimal valorCaucao;
    private final boolean assinadoPeloLocador;
    private final boolean assinadoPeloLocatario;
    private final boolean vigente;
    private final LocalDateTime dataEmissao;

    public ContratoResponse(boolean sucesso, String mensagem, String id, String locacaoId, String numero,
                            String nomeLocador, String nomeLocatario, String descricaoMaquina,
                            LocalDate dataInicio, LocalDate dataFim, long diarias,
                            BigDecimal valorTotal, BigDecimal valorCaucao,
                            boolean assinadoPeloLocador, boolean assinadoPeloLocatario,
                            boolean vigente, LocalDateTime dataEmissao) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.id = id;
        this.locacaoId = locacaoId;
        this.numero = numero;
        this.nomeLocador = nomeLocador;
        this.nomeLocatario = nomeLocatario;
        this.descricaoMaquina = descricaoMaquina;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.diarias = diarias;
        this.valorTotal = valorTotal;
        this.valorCaucao = valorCaucao;
        this.assinadoPeloLocador = assinadoPeloLocador;
        this.assinadoPeloLocatario = assinadoPeloLocatario;
        this.vigente = vigente;
        this.dataEmissao = dataEmissao;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getId() {
        return id;
    }

    public String getLocacaoId() {
        return locacaoId;
    }

    public String getNumero() {
        return numero;
    }

    public String getNomeLocador() {
        return nomeLocador;
    }

    public String getNomeLocatario() {
        return nomeLocatario;
    }

    public String getDescricaoMaquina() {
        return descricaoMaquina;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public long getDiarias() {
        return diarias;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getValorCaucao() {
        return valorCaucao;
    }

    public boolean isAssinadoPeloLocador() {
        return assinadoPeloLocador;
    }

    public boolean isAssinadoPeloLocatario() {
        return assinadoPeloLocatario;
    }

    public boolean isVigente() {
        return vigente;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }
}
