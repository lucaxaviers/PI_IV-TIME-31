/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Confere que o nome do método de listagem por usuário do LocacaoRepository usa campos
 *            que existem na entidade Locacao, sem precisar de banco.
 */

package com.alugamaquina.backend.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.repository.query.parser.PartTree;

import com.alugamaquina.backend.model.Locacao;

class LocacaoRepositoryListagemQueryTest {

    @Test
    // CT-LOC-21 | o nome do metodo de listagem por usuario e valido para a entidade Locacao
    void nomeDoMetodoDeListagemPorUsuarioDeveSerValido() {
        // Arrange
        String nomeDoMetodo = "findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc";

        // Act + Assert
        assertDoesNotThrow(() -> new PartTree(nomeDoMetodo, Locacao.class));
    }

    @Test
    // CT-LOC-22 | campo com erro de digitacao e rejeitado (o campo se chama locatarioId)
    void nomeComCampoInexistenteDeveSerRejeitado() {
        // Arrange
        String nomeErrado = "findByLocadorIdOrLocatariosIdOrderByDataSolicitacaoDesc";

        // Act + Assert
        assertThrows(PropertyReferenceException.class, () -> new PartTree(nomeErrado, Locacao.class));
    }
}
