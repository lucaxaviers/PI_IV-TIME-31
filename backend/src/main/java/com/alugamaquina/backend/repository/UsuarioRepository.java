/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Repositorio MongoDB para operacoes de persistencia e consulta de usuarios
*/

package com.alugamaquina.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.alugamaquina.backend.model.Usuario;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDocumento(String documento);

    boolean existsByEmail(String email);

    boolean existsByDocumento(String documento);
}

