/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Excecao lancada quando o id da maquina nao existe no banco.
*/

package com.alugamaquina.backend.exception;

// Erro de busca: nao existe maquina com esse id
public class MaquinaNaoEncontradaException extends RuntimeException {

    public MaquinaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
