/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Item do checklist trocado com a tela: descricao, resposta (conforme ou nao) e observacao
*/

package com.alugamaquina.backend.dto;

public class ItemChecklistDto {

    private String descricao;
    private Boolean conforme;
    private String observacao;

    public ItemChecklistDto() {
    }

    public ItemChecklistDto(String descricao, Boolean conforme, String observacao) {
        this.descricao = descricao;
        this.conforme = conforme;
        this.observacao = observacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getConforme() {
        return conforme;
    }

    public void setConforme(Boolean conforme) {
        this.conforme = conforme;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
