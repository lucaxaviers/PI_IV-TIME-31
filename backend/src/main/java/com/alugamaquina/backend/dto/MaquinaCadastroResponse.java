/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Resposta do cadastro de maquinas com o id gerado pelo MongoDB e os dados gravados
*/

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;

public class MaquinaCadastroResponse {

    private final boolean sucesso;
    private final String mensagem;
    private final String id;
    private final String tituloAnuncio;
    private final String tipoEquipamento;
    private final String municipio;
    private final BigDecimal valorDiaria;

    public MaquinaCadastroResponse(boolean sucesso, String mensagem, String id, String tituloAnuncio,
                                   String tipoEquipamento, String municipio, BigDecimal valorDiaria) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.id = id;
        this.tituloAnuncio = tituloAnuncio;
        this.tipoEquipamento = tipoEquipamento;
        this.municipio = municipio;
        this.valorDiaria = valorDiaria;
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

    @Override
    public String toString() {
        return "Maquina " + this.id + " - " + this.tituloAnuncio + " - " + this.municipio
                + " - R$ " + this.valorDiaria;
    }
}
