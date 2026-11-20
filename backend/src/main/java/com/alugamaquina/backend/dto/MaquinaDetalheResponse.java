/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Dados da maquina devolvidos pelo GET /api/maquinas/{id} para a tela de detalhe.
*/

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;

public class MaquinaDetalheResponse {

    private final String id;
    private final String tituloAnuncio;
    private final String tipoEquipamento;
    private final String municipio;
    private final BigDecimal valorDiaria;
    private final double latitude;
    private final double longitude;

    public MaquinaDetalheResponse(String id, String tituloAnuncio, String tipoEquipamento,
                                   String municipio, BigDecimal valorDiaria,
                                   double latitude, double longitude) {
        this.id = id;
        this.tituloAnuncio = tituloAnuncio;
        this.tipoEquipamento = tipoEquipamento;
        this.municipio = municipio;
        this.valorDiaria = valorDiaria;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getTituloAnuncio() {
        return tituloAnuncio;
    }

    public String getTipoEquipamento() {
        return tipoEquipamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
