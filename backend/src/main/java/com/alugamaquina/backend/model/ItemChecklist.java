/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Item do checklist da vistoria tecnica (descricao, se esta conforme e observacao) com os metodos canonicos
*/

package com.alugamaquina.backend.model;

import java.util.Objects;

public class ItemChecklist implements Comparable<ItemChecklist>, Cloneable {

    private String descricao;

    // null = ainda nao respondido; true = conforme; false = nao conforme
    private Boolean conforme;

    private String observacao;

    // Construtor vazio: o Spring Data usa para ler do banco
    public ItemChecklist() {
    }

    // Item novo: nasce sem resposta
    public ItemChecklist(String descricao) throws Exception {
        if (!ItemChecklist.isTextoValido(descricao))
            throw new Exception("Descricao do item ausente");

        this.descricao = descricao.trim();
    }

    // ---- Validacao estatica ----

    // Texto nao pode ser nulo nem so espacos
    public static boolean isTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    // ---- Getters e setters (os setters validam os dados) ----

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) throws Exception {
        if (!ItemChecklist.isTextoValido(descricao))
            throw new Exception("Descricao do item ausente");
        this.descricao = descricao.trim();
    }

    public Boolean getConforme() {
        return conforme;
    }

    public String getObservacao() {
        return observacao;
    }

    // ---- Metodos que alteram o this (verbo no imperativo) ----

    // Registra a resposta do item; item nao conforme exige observacao
    public void responda(boolean conforme, String observacao) throws Exception {
        if (!conforme && !ItemChecklist.isTextoValido(observacao))
            throw new Exception("Item nao conforme exige observacao");

        this.conforme = conforme;
        this.observacao = ItemChecklist.isTextoValido(observacao) ? observacao.trim() : null;
    }

    // ---- Predicativas ----

    public boolean isRespondido() {
        return this.conforme != null;
    }

    public boolean isNaoConforme() {
        return this.conforme != null && !this.conforme;
    }

    // ---- Metodos canonicos ----

    // Construtor de copia
    public ItemChecklist(ItemChecklist modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.descricao = modelo.descricao;
        this.conforme = modelo.conforme;      // Boolean e imutavel
        this.observacao = modelo.observacao;
    }

    @Override
    public Object clone() {
        ItemChecklist retorno = null;

        try {
            retorno = new ItemChecklist(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        String situacao = this.conforme == null ? "nao respondido" : (this.conforme ? "conforme" : "NAO conforme");
        return this.descricao + " - " + situacao
                + (this.observacao == null ? "" : " (" + this.observacao + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        ItemChecklist item = (ItemChecklist) obj;

        if (!Objects.equals(this.descricao, item.descricao)) return false;
        if (!Objects.equals(this.conforme, item.conforme)) return false;
        if (!Objects.equals(this.observacao, item.observacao)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.descricao == null ? 0 : this.descricao.hashCode());
        retorno = retorno * 2 + (this.conforme == null ? 0 : this.conforme.hashCode());
        retorno = retorno * 2 + (this.observacao == null ? 0 : this.observacao.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    // Ordem natural: pela descricao, em ordem alfabetica
    @Override
    public int compareTo(ItemChecklist item) {
        if (this.descricao == null && item.descricao == null) return 0;
        if (this.descricao == null) return -666;
        if (item.descricao == null) return 666;

        int comparacao = this.descricao.compareTo(item.descricao);
        if (comparacao < 0) return -666;
        if (comparacao > 0) return 666;
        return 0;
    }
}
