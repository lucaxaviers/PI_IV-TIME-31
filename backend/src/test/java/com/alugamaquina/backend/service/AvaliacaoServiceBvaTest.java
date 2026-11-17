/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes JUnit 5 de AvaliacaoService com Particionamento de Equivalencia e
             Analise de Valores de Fronteira (BVA), no padrao AAA (Issue #46)

  Particoes da nota (1 a 5):  invalida baixa (<= 0) | valida (1..5) | invalida alta (>= 6)
  Fronteiras testadas:        0, 1, 5, 6
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.alugamaquina.backend.dto.AvaliacaoRequest;
import com.alugamaquina.backend.dto.AvaliacaoResponse;
import com.alugamaquina.backend.model.Avaliacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.AvaliacaoRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

class AvaliacaoServiceBvaTest {

    private AvaliacaoRepository avaliacaoRepository;
    private UsuarioRepository usuarioRepository;
    private AvaliacaoService service;

    @BeforeEach
    void setUp() {
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AvaliacaoService(avaliacaoRepository, usuarioRepository);
    }

    /** Monta um pedido com a mesma nota nos 5 criterios, exceto o criterio indicado (0 a 4). */
    private AvaliacaoRequest pedido(int notaPadrao, int criterio, int notaDoCriterio) {
        int[] n = new int[5];
        for (int i = 0; i < 5; i++) n[i] = (i == criterio) ? notaDoCriterio : notaPadrao;
        return new AvaliacaoRequest("loc-1", "locador-1", "locatario-1", "maq-1",
                n[0], n[1], n[2], n[3], n[4], "ok", true);
    }

    private void prepararBancoComUmaAvaliacao(double mediaGravada) {
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));
        Avaliacao gravada = new Avaliacao("loc-1", "locador-1", "locatario-1", "maq-1",
                1, 1, 1, 1, 1, mediaGravada, "ok", true);
        when(avaliacaoRepository.findByLocadorId("locador-1")).thenReturn(List.of(gravada));
        when(usuarioRepository.findById("locador-1")).thenReturn(Optional.of(new Usuario()));
    }

    // ---------- Fronteiras invalidas: 0 e 6 (e valores bem fora) em cada um dos 5 criterios ----------

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 6, 100})
    void notaForaDoIntervaloDeveLancarExcecaoEmQualquerCriterio(int notaInvalida) {
        for (int criterio = 0; criterio < 5; criterio++) {
            // Arrange
            AvaliacaoRequest request = pedido(3, criterio, notaInvalida);

            // Action + Assert
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> service.registrarAvaliacao(request));
            assertEquals(true, erro.getMessage().contains("entre 1 e 5"));
        }
    }

    // ---------- Fronteiras validas: 1 e 5 ----------

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void notaNaFronteiraValidaNaoDeveLancarExcecao(int notaValida) {
        // Arrange
        prepararBancoComUmaAvaliacao(notaValida);

        for (int criterio = 0; criterio < 5; criterio++) {
            AvaliacaoRequest request = pedido(3, criterio, notaValida);

            // Action + Assert
            assertDoesNotThrow(() -> service.registrarAvaliacao(request));
        }
    }

    // ---------- Oraculo da media (tabela entrada -> saida esperada) ----------

    @Test
    void mediaDeTodasAsNotasMinimasDeveSerUm() {
        // Arrange
        prepararBancoComUmaAvaliacao(1.0);
        AvaliacaoRequest request = pedido(1, 0, 1);

        // Action
        AvaliacaoResponse response = service.registrarAvaliacao(request);

        // Assert
        assertEquals(1.0, response.getMediaFinalAvaliacao());
    }

    @Test
    void mediaDeTodasAsNotasMaximasDeveSerCinco() {
        // Arrange
        prepararBancoComUmaAvaliacao(5.0);
        AvaliacaoRequest request = pedido(5, 0, 5);

        // Action
        AvaliacaoResponse response = service.registrarAvaliacao(request);

        // Assert
        assertEquals(5.0, response.getMediaFinalAvaliacao());
    }

    @Test
    void mediaDeveSerArredondadaParaDuasCasas() {
        // Arrange: 1+2+2+2+2 = 9 -> 9/5 = 1.80 ; 5+5+5+5+4 = 24 -> 4.80 ; 1+1+1+1+2 = 6 -> 1.20
        prepararBancoComUmaAvaliacao(1.0);

        // Action
        double media = service.registrarAvaliacao(
                new AvaliacaoRequest("loc-1", "locador-1", "l-1", "m-1", 1, 2, 2, 2, 2, "", false))
                .getMediaFinalAvaliacao();

        // Assert
        assertEquals(1.80, media);
    }

    // ---------- Particao de entrada textual: locacao e locador ----------

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void locacaoEmBrancoDeveLancarExcecao(String locacaoVazia) {
        // Arrange
        AvaliacaoRequest request = new AvaliacaoRequest(locacaoVazia, "locador-1", "l-1", "m-1",
                3, 3, 3, 3, 3, "", true);

        // Action + Assert
        assertThrows(IllegalArgumentException.class, () -> service.registrarAvaliacao(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void locadorEmBrancoDeveLancarExcecao(String locadorVazio) {
        // Arrange
        AvaliacaoRequest request = new AvaliacaoRequest("loc-1", locadorVazio, "l-1", "m-1",
                3, 3, 3, 3, 3, "", true);

        // Action + Assert
        assertThrows(IllegalArgumentException.class, () -> service.registrarAvaliacao(request));
    }

    // ---------- Reputacao: particoes por quantidade de avaliacoes (0, 1, varias) ----------

    @Test
    void reputacaoComVariasAvaliacoesDeveSerAMediaDasMedias() {
        // Arrange: medias 2.0, 3.0 e 5.0 -> (2+3+5)/3 = 3.33
        when(avaliacaoRepository.findByLocadorId("locador-1")).thenReturn(List.of(
                avaliacaoComMedia(2.0), avaliacaoComMedia(3.0), avaliacaoComMedia(5.0)));
        Usuario locador = new Usuario();
        when(usuarioRepository.findById("locador-1")).thenReturn(Optional.of(locador));

        // Action
        double reputacao = service.recalcularReputacaoLocador("locador-1");

        // Assert
        assertEquals(3.33, reputacao);
        assertEquals(3.33, locador.getReputacaoMedia());
        assertEquals(3, locador.getTotalAvaliacoes());
    }

    @Test
    void reputacaoComLocadorInexistenteNaoDeveLancarExcecao() {
        // Arrange
        when(avaliacaoRepository.findByLocadorId("fantasma")).thenReturn(List.of(avaliacaoComMedia(4.0)));
        when(usuarioRepository.findById("fantasma")).thenReturn(Optional.empty());

        // Action + Assert
        assertEquals(4.0, service.recalcularReputacaoLocador("fantasma"));
    }

    private Avaliacao avaliacaoComMedia(double media) {
        return new Avaliacao("loc", "locador-1", "l-1", "m-1", 3, 3, 3, 3, 3, media, "", true);
    }
}
