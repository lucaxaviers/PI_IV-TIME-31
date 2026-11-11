/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Excecao lancada na tentativa de cadastrar usuario com email ja existente
*/

package com.alugamaquina.backend.exception;

public class UsuarioJaCadastradoException extends RuntimeException {

    public UsuarioJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}

