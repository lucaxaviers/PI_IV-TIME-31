/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Corpo do pedido para abrir a vistoria tecnica de uma locacao aceita
*/

package com.alugamaquina.backend.dto;

import com.alugamaquina.backend.model.TipoVistoria;

public class CriarVistoriaRequest {

    private String locacaoId;
    private TipoVistoria tipo;
    private String responsavelId;

    public String getLocacaoId() {
        return locacaoId;
    }

    public void setLocacaoId(String locacaoId) {
        this.locacaoId = locacaoId;
    }

    public TipoVistoria getTipo() {
        return tipo;
    }

    public void setTipo(TipoVistoria tipo) {
        this.tipo = tipo;
    }

    public String getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(String responsavelId) {
        this.responsavelId = responsavelId;
    }
}
