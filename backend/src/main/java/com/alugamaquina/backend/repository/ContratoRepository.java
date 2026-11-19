/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Repositório MongoDB dos contratos, com consultas por locação.
*/

package com.alugamaquina.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.alugamaquina.backend.model.Contrato;

@Repository
public interface ContratoRepository extends MongoRepository<Contrato, String> {

    // Cada locacao tem no maximo um contrato
    Optional<Contrato> findByLocacaoId(String locacaoId);

    boolean existsByLocacaoId(String locacaoId);
}
