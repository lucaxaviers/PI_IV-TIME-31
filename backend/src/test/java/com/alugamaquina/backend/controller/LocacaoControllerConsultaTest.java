/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Testes JUnit 5 dos endpoints GET do LocacaoController (consulta por id e por usuário)
 *            e do status HTTP de cada erro, com o service falso (mock) e no padrão AAA.
 */

package com.alugamaquina.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.alugamaquina.backend.dto.LocacaoResponse;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.service.LocacaoService;

class LocacaoControllerConsultaTest {

    private LocacaoService service;
    private LocacaoController controller;

    @BeforeEach
    // Cria o controller com o service falso (mock)
    void setUp() {
        service = mock(LocacaoService.class);
        controller = new LocacaoController(service);
    }

    private LocacaoResponse resposta(String id) {
        return new LocacaoResponse(true, "Locacao encontrada.", id, "maq-1", StatusLocacao.PENDENTE,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14), 5,
                new BigDecimal("850.00"), new BigDecimal("475.00"), new BigDecimal("4725.00"), null,
                "locador-1", "locatario-1", "Trator John Deere 6115J", "Maria Souza");
    }

    @Test
    // CT-LOC-16 | GET /{id} com id existente responde 200 com a locacao
    void deveResponder200AoBuscarPorId() {
        // Arrange
        LocacaoResponse esperada = resposta("loc-1");
        when(service.buscarPorId("loc-1")).thenReturn(esperada);

        // Act
        ResponseEntity<LocacaoResponse> resultado = controller.buscarPorId("loc-1");

        // Assert
        assertEquals(200, resultado.getStatusCode().value());
        assertSame(esperada, resultado.getBody());
    }

    @Test
    // CT-LOC-17 | GET /usuario/{usuarioId} responde 200 com a lista na ordem do service
    void deveResponder200ComAListaDoUsuario() {
        // Arrange
        when(service.listarPorUsuario("u-1")).thenReturn(List.of(resposta("loc-2"), resposta("loc-1")));

        // Act
        ResponseEntity<List<LocacaoResponse>> resultado = controller.listarPorUsuario("u-1");

        // Assert
        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(2, resultado.getBody().size());
        assertEquals("loc-2", resultado.getBody().get(0).getId());
    }

    @Test
    // CT-LOC-18 | usuario sem locacoes: 200 com lista vazia (nao e erro)
    void deveResponder200ComListaVaziaQuandoNaoHaLocacoes() {
        // Arrange
        when(service.listarPorUsuario("u-sem-locacao")).thenReturn(List.of());

        // Act
        ResponseEntity<List<LocacaoResponse>> resultado = controller.listarPorUsuario("u-sem-locacao");

        // Assert
        assertEquals(200, resultado.getStatusCode().value());
        assertTrue(resultado.getBody().isEmpty());
    }

    @Test
    // CT-LOC-19 | locacao inexistente: o tratador devolve 404 com sucesso=false e a mensagem
    void deveResponder404QuandoLocacaoNaoExiste() {
        // Arrange
        LocacaoNaoEncontradaException erro = new LocacaoNaoEncontradaException("Locacao nao encontrada: x");

        // Act
        ResponseEntity<Map<String, Object>> resultado = controller.tratarNaoEncontrada(erro);

        // Assert
        assertEquals(404, resultado.getStatusCode().value());
        assertFalse((Boolean) resultado.getBody().get("sucesso"));
        assertEquals("Locacao nao encontrada: x", resultado.getBody().get("erro"));
    }

    @Test
    // CT-LOC-20 | id ou usuario invalido: o tratador devolve 400
    void deveResponder400QuandoIdForInvalido() {
        // Arrange
        IllegalArgumentException erro = new IllegalArgumentException("Identificador do usuario e obrigatorio.");

        // Act
        ResponseEntity<Map<String, Object>> resultado = controller.tratarArgumentoInvalido(erro);

        // Assert
        assertEquals(400, resultado.getStatusCode().value());
        assertEquals("Identificador do usuario e obrigatorio.", resultado.getBody().get("erro"));
    }
}
