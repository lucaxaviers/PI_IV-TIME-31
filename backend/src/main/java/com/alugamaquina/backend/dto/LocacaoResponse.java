/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: DTO de resposta com a locação, o número de diárias, o frete e o valor total.
 */

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.alugamaquina.backend.model.StatusLocacao;

public class LocacaoResponse {

    private final boolean sucesso;
    private final String mensagem;
    private final String id;
    private final String maquinaId;
    private final StatusLocacao status;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final long diarias;
    private final BigDecimal valorDiaria;
    private final BigDecimal valorFrete;
    private final BigDecimal valorTotal;
    private final String motivoRecusa;

    public LocacaoResponse(boolean sucesso, String mensagem, String id, String maquinaId,
                           StatusLocacao status, LocalDate dataInicio, LocalDate dataFim,
                           long diarias, BigDecimal valorDiaria, BigDecimal valorFrete,
                           BigDecimal valorTotal, String motivoRecusa) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.id = id;
        this.maquinaId = maquinaId;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.diarias = diarias;
        this.valorDiaria = valorDiaria;
        this.valorFrete = valorFrete;
        this.valorTotal = valorTotal;
        this.motivoRecusa = motivoRecusa;
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

    public String getMaquinaId() {
        return maquinaId;
    }

    public StatusLocacao getStatus() {
        return status;
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

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }
}
