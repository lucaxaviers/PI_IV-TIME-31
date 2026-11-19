/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Repositório MongoDB das máquinas, usado para ler o equipamento que entra no contrato.
*/

package com.alugamaquina.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.alugamaquina.backend.model.Maquina;

@Repository
public interface MaquinaRepository extends MongoRepository<Maquina, String> {
}
