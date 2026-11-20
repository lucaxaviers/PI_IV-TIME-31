/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Repositório MongoDB das locações, com a consulta de conflito de agenda da máquina
 *            e a listagem das locações de um usuário.
 */

package com.alugamaquina.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.StatusLocacao;

@Repository
public interface LocacaoRepository extends MongoRepository<Locacao, String> {

    // Existe reserva com esse status que bate com o periodo? Dois periodos se cruzam quando
    // dataInicio existente <= fim novo E dataFim existente >= inicio novo (por isso fim vem antes de inicio)
    boolean existsByMaquinaIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            String maquinaId, StatusLocacao status, LocalDate fimNovo, LocalDate inicioNovo);

    // Locacoes em que o usuario e locador OU locatario, da mais nova para a mais antiga
    List<Locacao> findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc(String locadorId, String locatarioId);
}
