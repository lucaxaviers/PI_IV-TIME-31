/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Excecao lancada quando latitude, longitude ou raio de busca sao invalidos (Issue #36)
*/

package com.alugamaquina.backend.exception;

public class ParametroBuscaInvalidoException extends RuntimeException {

    public ParametroBuscaInvalidoException(String mensagem) {
        super(mensagem);
    }
}
