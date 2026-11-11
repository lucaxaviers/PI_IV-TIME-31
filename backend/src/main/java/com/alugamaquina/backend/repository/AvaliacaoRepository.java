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

    List<Avaliacao> findByLocadorId(String locadorId);

    List<Avaliacao> findByMaquinaId(String maquinaId);

    boolean existsByLocacaoId(String locacaoId);
}
