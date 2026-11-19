/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes OO Interclasse (End-to-End no servidor Java) do fluxo de Avaliacao Pos-Locacao
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.alugamaquina.backend.controller.AvaliacaoController;
import com.alugamaquina.backend.dto.AvaliacaoRequest;
import com.alugamaquina.backend.dto.AvaliacaoResponse;
import com.alugamaquina.backend.model.Avaliacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.AvaliacaoRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

/**
 * Testes OO Interclasse integrando:
 * AvaliacaoController -> AvaliacaoService -> AvaliacaoRequest -> Avaliacao ->
 * AvaliacaoRepository -> UsuarioRepository -> Usuario -> AvaliacaoResponse.
 */
class AvaliacaoInterclasseTest {

    private AvaliacaoRepository avaliacaoRepository;
    private UsuarioRepository usuarioRepository;
    private AvaliacaoService avaliacaoService;
    private AvaliacaoController avaliacaoController;

    @BeforeEach
    void setUp() {
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        avaliacaoService = new AvaliacaoService(avaliacaoRepository, usuarioRepository);
        avaliacaoController = new AvaliacaoController(avaliacaoService);
    }

    // Cenario NORMAL: chamada passa pelo Controller, Service, cria Avaliacao, recalcula e atualiza Usuario
    @Test
    void cenarioNormalDeveIntegrarControllerServiceEntidadesERetornarHttp201() {
        // Arrange
        AvaliacaoRequest request = new AvaliacaoRequest(
                "loc-200",
                "locador-10",
                "locatario-20",
                "maq-trator-01",
                5, 4, 5, 4, 5, // media individual = 23 / 5 = 4.60
                "Maquina revisada e entregue no horario combinado.",
                true
        );

        Usuario locador = new Usuario(
                "Fazenda Sao Joao", "12345678000190", "19999990000",
                "contato@fazendasj.com", "Paulinia", "locador", "hash"
        );
        locador.setId("locador-10");
        locador.setReputacaoMedia(5.0);
        locador.setTotalAvaliacoes(1);

        Avaliacao avaliacaoAnterior = new Avaliacao(
                "loc-150", "locador-10", "locatario-18", "maq-trator-01",
                5, 5, 5, 5, 5, 5.0, "Excelente", true
        );

        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> {
            Avaliacao nova = inv.getArgument(0);
            nova.setId("aval-200");
            return nova;
        });

        // Simula retorno do historico contendo a avaliacao anterior (5.0) e a recem-salva (4.60)
        when(avaliacaoRepository.findByLocadorId("locador-10")).thenAnswer(inv -> {
            Avaliacao atual = new Avaliacao(
                    "loc-200", "locador-10", "locatario-20", "maq-trator-01",
                    5, 4, 5, 4, 5, 4.60, "Maquina revisada e entregue no horario combinado.", true
            );
            return List.of(avaliacaoAnterior, atual);
        });

        when(usuarioRepository.findById("locador-10")).thenReturn(Optional.of(locador));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Action: aciona o servico principal a partir da camada Controller
        ResponseEntity<AvaliacaoResponse> httpResponse = avaliacaoController.registrarAvaliacao(request);

        // Assert: verifica devolucao HTTP, DTO de resposta, entidade Usuario atualizada e colaboracoes
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.CREATED, httpResponse.getStatusCode());

        AvaliacaoResponse body = httpResponse.getBody();
        assertNotNull(body);
        assertTrue(body.isSucesso());
        assertEquals("aval-200", body.getId());
        assertEquals("loc-200", body.getLocacaoId());
        assertEquals("locador-10", body.getLocadorId());
        assertEquals(4.60, body.getMediaFinalAvaliacao());
        assertEquals(4.80, body.getNovaReputacaoLocador()); // (5.00 + 4.60) / 2
        assertEquals(2, body.getTotalAvaliacoesLocador());

        // Verifica atualizacao de estado na entidade Usuario colaboradora
        assertEquals(4.80, locador.getReputacaoMedia());
        assertEquals(2, locador.getTotalAvaliacoes());

        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
        verify(usuarioRepository, times(1)).findById("locador-10");
        verify(usuarioRepository, times(1)).save(locador);
        // Documenta anomalia: existsByLocacaoId existe no repositorio, mas nao e consultado
        verify(avaliacaoRepository, never()).existsByLocacaoId(any());
    }

    // Cenario DE ERRO: nota invalida interrompe fluxo no Service com IllegalArgumentException e Controller converte em HTTP 400
    @Test
    void cenarioErroNotaInvalidaDeveLancarExcecaoENaoPersistirGerandoHttp400NoController() {
        // Arrange
        AvaliacaoRequest requestInvalido = new AvaliacaoRequest(
                "loc-201",
                "locador-10",
                "locatario-20",
                "maq-trator-01",
                0, 4, 5, 4, 5, // notaDesempenho = 0 viola faixa 1..5
                "Tentativa com nota invalida",
                false
        );

        // Action 1: dispara chamada pelo Controller e valida excecao propagada pelo Service
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> avaliacaoController.registrarAvaliacao(requestInvalido)
        );

        // Action 2: simula o tratamento global do Controller (@ExceptionHandler)
        ResponseEntity<Map<String, Object>> httpErro = avaliacaoController.tratarValidacao(excecao);

        // Assert: verifica mensagem, status HTTP 400 e ausencia de persistencia
        assertTrue(excecao.getMessage().contains("entre 1 e 5"));
        assertEquals(HttpStatus.BAD_REQUEST, httpErro.getStatusCode());
        assertNotNull(httpErro.getBody());
        assertFalse((Boolean) httpErro.getBody().get("sucesso"));
        assertEquals(excecao.getMessage(), httpErro.getBody().get("erro"));

        verify(avaliacaoRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }
}
