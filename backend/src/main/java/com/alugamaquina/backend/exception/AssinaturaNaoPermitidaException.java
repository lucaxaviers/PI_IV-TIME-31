/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Exceção lançada quando quem tenta assinar não é uma das partes do contrato.
*/

package com.alugamaquina.backend.exception;

// Erro 403: o usuario nao e locador nem locatario deste contrato
public class AssinaturaNaoPermitidaException extends RuntimeException {

    public AssinaturaNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
