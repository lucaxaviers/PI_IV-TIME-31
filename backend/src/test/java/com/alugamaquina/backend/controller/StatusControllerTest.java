/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes unitarios com JUnit 5 para o StatusController
*/

package com.alugamaquina.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/**
 * Teste unitario basico com JUnit 5 para validar o endpoint de status do servidor.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #21)
 */
class StatusControllerTest {

    private final StatusController controller = new StatusController();

    @Test
    void deveRetornarStatusOnlineDoServidor() {
        ResponseEntity<Map<String, Object>> resposta = controller.verificarStatus();

        assertNotNull(resposta);
        assertEquals(200, resposta.getStatusCode().value());
        assertNotNull(resposta.getBody());
        assertEquals("ONLINE", resposta.getBody().get("status"));
        assertEquals("AlugaMaquina API", resposta.getBody().get("sistema"));
        assertEquals("0.1.0", resposta.getBody().get("versao"));
    }
}

