/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: DTO com os dados enviados pelo front-end para solicitar a locação de uma máquina.
 */

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SolicitacaoLocacaoRequest {

    private String locadorId;
    private String locatarioId;
    private String maquinaId;

    // Datas no formato ISO (ex.: 2026-11-10)
    private LocalDate dataInicio;
    private LocalDate dataFim;

    private BigDecimal valorDiaria;

    // Distancia da entrega em km (0 = retirada no local, sem frete)
    private double distanciaKm;

    // Construtor vazio: o Spring usa para ler o JSON
    public SolicitacaoLocacaoRequest() {
    }

    // Construtor com todos os campos (usado nos testes)
    public SolicitacaoLocacaoRequest(String locadorId, String locatarioId, String maquinaId,
                                     LocalDate dataInicio, LocalDate dataFim,
                                     BigDecimal valorDiaria, double distanciaKm) {
        this.locadorId = locadorId;
        this.locatarioId = locatarioId;
        this.maquinaId = maquinaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valorDiaria = valorDiaria;
        this.distanciaKm = distanciaKm;
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

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
}
