/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: DTO de resposta para cada maquina retornada na busca geoespacial (Issue #36)
*/

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;

public class MaquinaBuscaResponse {

    // Dados exibidos nos cards do catalogo junto com a distancia calculada pelo geoNear
    private final String id;
    private final String tituloAnuncio;
    private final String tipoEquipamento;
    private final String municipio;
    private final BigDecimal valorDiaria;
    private final double distanciaKm;

    // Construtor completo usado ao mapear cada resultado da busca geoespacial
    public MaquinaBuscaResponse(String id, String tituloAnuncio, String tipoEquipamento,
                                 String municipio, BigDecimal valorDiaria, double distanciaKm) {
        this.id = id;
        this.tituloAnuncio = tituloAnuncio;
        this.tipoEquipamento = tipoEquipamento;
        this.municipio = municipio;
        this.valorDiaria = valorDiaria;
        this.distanciaKm = distanciaKm;
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

    public double getDistanciaKm() {
        return distanciaKm;
    }
}
