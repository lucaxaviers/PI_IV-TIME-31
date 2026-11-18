/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Exceção lançada ao tentar aceitar ou recusar uma locação que não está pendente.
 */

package com.alugamaquina.backend.exception;

// Erro de fluxo: so uma locacao PENDENTE pode ser aceita ou recusada
public class TransicaoStatusInvalidaException extends RuntimeException {

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
