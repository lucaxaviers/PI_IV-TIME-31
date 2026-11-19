/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Corpo do pedido de assinatura, com o usuário que está assinando.
*/

package com.alugamaquina.backend.dto;

public class AssinarContratoRequest {

    private String usuarioId;

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
