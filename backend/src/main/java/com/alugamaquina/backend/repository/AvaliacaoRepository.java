/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Repositorio MongoDB para persistencia e consulta de avaliacoes
*/

package com.alugamaquina.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.alugamaquina.backend.model.Avaliacao;

/**
 * Repositorio Spring Data MongoDB para a colecao de avaliacoes.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
@Repository
public interface AvaliacaoRepository extends MongoRepository<Avaliacao, String> {

    // Avaliacoes recebidas por um locador (base da reputacao)
    List<Avaliacao> findByLocadorId(String locadorId);

    // Avaliacoes de uma maquina
    List<Avaliacao> findByMaquinaId(String maquinaId);

    // Garante uma avaliacao por locacao
    boolean existsByLocacaoId(String locacaoId);
}
