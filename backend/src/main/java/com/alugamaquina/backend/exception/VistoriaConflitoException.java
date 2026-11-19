/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Excecao lancada quando a acao conflita com o estado atual (locacao nao aceita, vistoria repetida ou ja concluida)
*/

package com.alugamaquina.backend.exception;

// Erro 409: o pedido esta correto, mas nao pode ser feito agora
public class VistoriaConflitoException extends RuntimeException {

    public VistoriaConflitoException(String mensagem) {
        super(mensagem);
    }
}
