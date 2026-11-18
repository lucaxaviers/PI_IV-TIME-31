/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Estados possíveis de uma locação, do pedido do locatário até a devolução da máquina.
 */

package com.alugamaquina.backend.model;

public enum StatusLocacao {
    PENDENTE,
    ACEITO,
    RECUSADO,
    CANCELADO,
    FINALIZADO
}
