package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alugamaquina.backend.dto.AvaliacaoRequest;
import com.alugamaquina.backend.dto.AvaliacaoResponse;
import com.alugamaquina.backend.model.Avaliacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.AvaliacaoRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

/**
 * Testes unitarios com JUnit 5 e Mockito para as regras de avaliacao e reputacao.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
class AvaliacaoServiceTest {

    private AvaliacaoRepository avaliacaoRepository;
    private UsuarioRepository usuarioRepository;
    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void setUp() {
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        avaliacaoService = new AvaliacaoService(avaliacaoRepository, usuarioRepository);
    }

    @Test
    void deveRegistrarAvaliacaoERecalcularReputacao() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                "loc-100",
                "locador-1",
                "locatario-2",
                "maq-5",
                5, 4, 5, 4, 5, // Media das 5 notas: 23 / 5 = 4.60
                "Excelente trator, entregue no horario e muito bem conservado.",
                true
        );

        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> {
            Avaliacao a = invocation.getArgument(0);
            a.setId("aval-generated-1");
            return a;
        });

        // Simula historico de 2 avaliacoes do locador (uma com media 4.60 e outra com media 5.0)
        Avaliacao anterior = new Avaliacao("loc-99", "locador-1", "locatario-3", "maq-5",
                5, 5, 5, 5, 5, 5.0, "Perfeito", true);
        Avaliacao atual = new Avaliacao("loc-100", "locador-1", "locatario-2", "maq-5",
                5, 4, 5, 4, 5, 4.6, "Muito bom", true);

        when(avaliacaoRepository.findByLocadorId("locador-1")).thenReturn(List.of(anterior, atual));

        Usuario locadorMock = new Usuario();
        locadorMock.setId("locador-1");
        locadorMock.setNome("Fazenda Santa Maria");
        when(usuarioRepository.findById("locador-1")).thenReturn(Optional.of(locadorMock));

        AvaliacaoResponse response = avaliacaoService.registrarAvaliacao(request);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals("aval-generated-1", response.getId());
        assertEquals(4.60, response.getMediaFinalAvaliacao());
        // Nova reputacao geral: (5.0 + 4.6) / 2 = 4.80
        assertEquals(4.80, response.getNovaReputacaoLocador());
        assertEquals(2, response.getTotalAvaliacoesLocador());
    }

    @Test
    void deveLancarExcecaoParaNotaAbaixoDeUm() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                "loc-100",
                "locador-1",
                "locatario-2",
                "maq-5",
                0, 4, 5, 4, 5, // Nota 0 invalida
                "Comentario",
                true
        );

        assertThrows(IllegalArgumentException.class, () -> avaliacaoService.registrarAvaliacao(request));
    }

    @Test
    void deveLancarExcecaoParaNotaAcimaDeCinco() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                "loc-100",
                "locador-1",
                "locatario-2",
                "maq-5",
                5, 6, 5, 4, 5, // Nota 6 invalida
                "Comentario",
                true
        );

        assertThrows(IllegalArgumentException.class, () -> avaliacaoService.registrarAvaliacao(request));
    }

    @Test
    void deveLancarExcecaoQuandoLocacaoNaoInformada() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                "", // Locacao vazia
                "locador-1",
                "locatario-2",
                "maq-5",
                5, 5, 5, 5, 5,
                "Comentario",
                true
        );

        assertThrows(IllegalArgumentException.class, () -> avaliacaoService.registrarAvaliacao(request));
    }

    @Test
    void recalcularReputacaoLocadorSemAvaliacoesRetornaPadraoNeutro() {
        when(avaliacaoRepository.findByLocadorId("locador-novo")).thenReturn(List.of());

        double reputacao = avaliacaoService.recalcularReputacaoLocador("locador-novo");

        assertEquals(5.0, reputacao);
    }
}
