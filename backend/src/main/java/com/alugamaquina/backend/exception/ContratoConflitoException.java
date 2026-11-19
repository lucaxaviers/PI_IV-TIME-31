/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Exceção lançada quando a ação conflita com o estado atual (locação não aceita, contrato já existente ou assinatura repetida).
*/

package com.alugamaquina.backend.exception;

// Erro 409: o pedido esta correto, mas nao pode ser feito agora
public class ContratoConflitoException extends RuntimeException {

    public ContratoConflitoException(String mensagem) {
        super(mensagem);
    }
}
