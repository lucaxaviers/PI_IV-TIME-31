package com.alugamaquina.backend.exception;

public class UsuarioJaCadastradoException extends RuntimeException {

    public UsuarioJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}

