/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Excecao lancada quando a vistoria pedida nao existe
*/

package com.alugamaquina.backend.exception;

// Erro 404: nao ha vistoria com esse id
public class VistoriaNaoEncontradaException extends RuntimeException {

    public VistoriaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
