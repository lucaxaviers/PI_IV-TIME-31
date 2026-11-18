/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Exceção lançada quando o id da locação não existe no banco.
 */

package com.alugamaquina.backend.exception;

// Erro de busca: nao existe locacao com esse id
public class LocacaoNaoEncontradaException extends RuntimeException {

    public LocacaoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
