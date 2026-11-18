/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Entidade Locacao (collection "locacoes") com os dados da reserva, valores e status,
 *            e os métodos canônicos equals, hashCode, compareTo, toString, cópia e clone.
 */

package com.alugamaquina.backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "locacoes")
public class Locacao implements Comparable<Locacao>, Cloneable {

    @Id
    private String id;

    // Ids que ligam a locacao as pessoas e a maquina
    private String locadorId;
    private String locatarioId;
    private String maquinaId;

    // Periodo da reserva (os dois dias contam como diaria)
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Valores em reais, sempre com 2 casas decimais
    private BigDecimal valorDiaria;
    private BigDecimal valorFrete;
    private BigDecimal valorTotal;

    private StatusLocacao status;
    private String motivoRecusa;
    private LocalDateTime dataSolicitacao;

    // Construtor vazio: o Spring Data usa para ler do banco
    public Locacao() {
        this.status = StatusLocacao.PENDENTE;
        this.dataSolicitacao = LocalDateTime.now();
    }

    // Construtor de uma nova solicitacao: nasce PENDENTE
    public Locacao(String locadorId, String locatarioId, String maquinaId,
                   LocalDate dataInicio, LocalDate dataFim,
                   BigDecimal valorDiaria, BigDecimal valorFrete, BigDecimal valorTotal) {
        this();
        this.locadorId = locadorId;
        this.locatarioId = locatarioId;
        this.maquinaId = maquinaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valorDiaria = normalizar(valorDiaria);
        this.valorFrete = normalizar(valorFrete);
        this.valorTotal = normalizar(valorTotal);
    }

    // Deixa todo valor com 2 casas: 850 e 850.00 viram o mesmo numero no equals e no hashCode
    private static BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.valorDiaria = normalizar(valorDiaria);
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = normalizar(valorFrete);
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = normalizar(valorTotal);
    }

    public StatusLocacao getStatus() {
        return status;
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }

    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    // ---- Metodos canonicos (padrao Prof. Francisco) ----

    public Locacao(Locacao modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.locadorId = modelo.locadorId;
        this.locatarioId = modelo.locatarioId;
        this.maquinaId = modelo.maquinaId;
        this.dataInicio = modelo.dataInicio;           // LocalDate e imutavel
        this.dataFim = modelo.dataFim;                 // LocalDate e imutavel
        this.valorDiaria = modelo.valorDiaria;         // BigDecimal e imutavel
        this.valorFrete = modelo.valorFrete;           // BigDecimal e imutavel
        this.valorTotal = modelo.valorTotal;           // BigDecimal e imutavel
        this.status = modelo.status;
        this.motivoRecusa = modelo.motivoRecusa;
        this.dataSolicitacao = modelo.dataSolicitacao; // LocalDateTime e imutavel
    }

    @Override
    public Object clone() {
        Locacao retorno = null;

        try {
            retorno = new Locacao(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return "Locacao " + this.id
                + " - maquina " + this.maquinaId
                + " - de " + this.dataInicio + " a " + this.dataFim
                + " - total R$ " + this.valorTotal
                + " - " + this.status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        Locacao loc = (Locacao) obj;

        if (!Objects.equals(this.id, loc.id)) return false;
        if (!Objects.equals(this.locadorId, loc.locadorId)) return false;
        if (!Objects.equals(this.locatarioId, loc.locatarioId)) return false;
        if (!Objects.equals(this.maquinaId, loc.maquinaId)) return false;
        if (!Objects.equals(this.dataInicio, loc.dataInicio)) return false;
        if (!Objects.equals(this.dataFim, loc.dataFim)) return false;
        if (!Objects.equals(this.valorDiaria, loc.valorDiaria)) return false;
        if (!Objects.equals(this.valorFrete, loc.valorFrete)) return false;
        if (!Objects.equals(this.valorTotal, loc.valorTotal)) return false;
        if (this.status != loc.status) return false;
        if (!Objects.equals(this.motivoRecusa, loc.motivoRecusa)) return false;
        if (!Objects.equals(this.dataSolicitacao, loc.dataSolicitacao)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.locadorId == null ? 0 : this.locadorId.hashCode());
        retorno = retorno * 2 + (this.locatarioId == null ? 0 : this.locatarioId.hashCode());
        retorno = retorno * 2 + (this.maquinaId == null ? 0 : this.maquinaId.hashCode());
        retorno = retorno * 2 + (this.dataInicio == null ? 0 : this.dataInicio.hashCode());
        retorno = retorno * 2 + (this.dataFim == null ? 0 : this.dataFim.hashCode());
        retorno = retorno * 2 + (this.valorDiaria == null ? 0 : this.valorDiaria.hashCode());
        retorno = retorno * 2 + (this.valorFrete == null ? 0 : this.valorFrete.hashCode());
        retorno = retorno * 2 + (this.valorTotal == null ? 0 : this.valorTotal.hashCode());
        retorno = retorno * 2 + (this.status == null ? 0 : this.status.name().hashCode());
        retorno = retorno * 2 + (this.motivoRecusa == null ? 0 : this.motivoRecusa.hashCode());
        retorno = retorno * 2 + (this.dataSolicitacao == null ? 0 : this.dataSolicitacao.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    // Ordem natural: pela data de inicio da locacao (a mais cedo vem primeiro)
    @Override
    public int compareTo(Locacao loc) {
        if (this.dataInicio == null && loc.dataInicio == null) return 0;
        if (this.dataInicio == null) return -666;
        if (loc.dataInicio == null) return 666;

        if (this.dataInicio.isBefore(loc.dataInicio)) return -666;
        if (this.dataInicio.isAfter(loc.dataInicio)) return 666;
        return 0;
    }
}
