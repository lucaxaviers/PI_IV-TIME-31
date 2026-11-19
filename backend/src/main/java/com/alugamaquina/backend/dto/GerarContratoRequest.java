/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Corpo do pedido para gerar o contrato de uma locação aceita.
*/

package com.alugamaquina.backend.dto;

public class GerarContratoRequest {

    private String locacaoId;

    public String getLocacaoId() {
        return locacaoId;
    }

    public void setLocacaoId(String locacaoId) {
        this.locacaoId = locacaoId;
    }
}
