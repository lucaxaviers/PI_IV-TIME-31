/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Repositorio MongoDB das vistorias tecnicas, com consultas por locacao e tipo
*/

package com.alugamaquina.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.alugamaquina.backend.model.TipoVistoria;
import com.alugamaquina.backend.model.VistoriaChecklist;

@Repository
public interface VistoriaChecklistRepository extends MongoRepository<VistoriaChecklist, String> {

    // Vistorias de uma locacao (retirada e devolucao)
    List<VistoriaChecklist> findByLocacaoId(String locacaoId);

    // Cada locacao tem no maximo uma vistoria de cada tipo
    boolean existsByLocacaoIdAndTipo(String locacaoId, TipoVistoria tipo);
}
