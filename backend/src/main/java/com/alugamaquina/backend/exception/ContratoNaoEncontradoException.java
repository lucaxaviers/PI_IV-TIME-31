/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Exceção lançada quando o contrato pedido não existe.
*/

package com.alugamaquina.backend.exception;

// Erro 404: nao ha contrato com esse id (ou para essa locacao)
public class ContratoNaoEncontradoException extends RuntimeException {

    public ContratoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
