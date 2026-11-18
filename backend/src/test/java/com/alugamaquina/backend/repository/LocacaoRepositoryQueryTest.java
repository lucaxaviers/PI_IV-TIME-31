/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Confere que o nome do método de conflito de agenda do LocacaoRepository usa campos
 *            que existem na entidade Locacao, sem precisar de banco (um erro de digitação
 *            só apareceria ao subir o servidor).
 */

package com.alugamaquina.backend.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.repository.query.parser.PartTree;

import com.alugamaquina.backend.model.Locacao;

class LocacaoRepositoryQueryTest {

    @Test
    void nomeDoMetodoDeConflitoDeAgendaDeveSerValido() {
        // Arrange
        String nomeDoMetodo = "existsByMaquinaIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual";

        // Act + Assert
        assertDoesNotThrow(() -> new PartTree(nomeDoMetodo, Locacao.class));
    }

    @Test
    void nomeComCampoInexistenteDeveSerRejeitado() {
        // Arrange: "dataTermino" nao existe na entidade (o campo se chama dataFim)
        String nomeErrado = "existsByMaquinaIdAndDataTerminoGreaterThanEqual";

        // Act + Assert
        assertThrows(PropertyReferenceException.class, () -> new PartTree(nomeErrado, Locacao.class));
    }
}
