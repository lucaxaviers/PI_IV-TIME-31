/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Excecao lancada em falhas de autenticacao e credenciais invalidas
*/

package com.alugamaquina.backend.exception;

public class AutenticacaoException extends RuntimeException {

    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }
}

