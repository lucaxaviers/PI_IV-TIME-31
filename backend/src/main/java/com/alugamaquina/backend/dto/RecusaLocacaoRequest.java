/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: DTO opcional com o motivo informado pelo locador ao recusar uma solicitação.
 */

package com.alugamaquina.backend.dto;

public class RecusaLocacaoRequest {

    private String motivo;

    // Construtor vazio: o Spring usa para ler o JSON
    public RecusaLocacaoRequest() {
    }

    public RecusaLocacaoRequest(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
