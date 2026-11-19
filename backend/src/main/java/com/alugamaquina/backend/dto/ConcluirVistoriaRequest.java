/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Corpo do pedido para concluir a vistoria, com o horimetro e a resposta de cada item
*/

package com.alugamaquina.backend.dto;

import java.util.List;

public class ConcluirVistoriaRequest {

    private Double horimetro;
    private List<ItemChecklistDto> itens;

    public Double getHorimetro() {
        return horimetro;
    }

    public void setHorimetro(Double horimetro) {
        this.horimetro = horimetro;
    }

    public List<ItemChecklistDto> getItens() {
        return itens;
    }

    public void setItens(List<ItemChecklistDto> itens) {
        this.itens = itens;
    }
}
