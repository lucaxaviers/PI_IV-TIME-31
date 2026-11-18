/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Exceção lançada quando a máquina já tem reserva aceita no período pedido.
 */

package com.alugamaquina.backend.exception;

// Erro de agenda: a maquina ja esta reservada nessas datas
public class ConflitoAgendaException extends RuntimeException {

    public ConflitoAgendaException(String mensagem) {
        super(mensagem);
    }
}
