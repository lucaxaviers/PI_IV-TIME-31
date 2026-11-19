/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes OO Intraclasse (Particionamento por Categorias de Metodos) da entidade Avaliacao
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Testes OO Intraclasse da classe de dominio Avaliacao usando Particionamento por
 * Categorias de Metodos (Inicializacao, Setup, Consulta, Predicativos e Computacao)
 * no padrao AAA (Arrange, Action, Assert).
 */
class AvaliacaoIntraclasseTest {

    private static final LocalDateTime DATA_FIXA = LocalDateTime.of(2026, 11, 10, 14, 30);

    // Categorias: Inicializacao -> Setup (setters) -> Consultas (getters/toString) -> Predicativo (isRecomenda)
    @Test
    void cicloDeVidaPorCategoriasDeInicializacaoSetupConsultaEPredicativo() {
        // Arrange (Inicializacao com construtor vazio)
        Avaliacao avaliacao = new Avaliacao();

        // Action (Setup de valores + Consultas + Predicativo)
        avaliacao.setId("av-1");
        avaliacao.setLocacaoId("loc-10");
        avaliacao.setLocadorId("usr-locador");
        avaliacao.setLocatarioId("usr-locatario");
        avaliacao.setMaquinaId("maq-colheitadeira-1");
        avaliacao.setNotaDesempenho(5);
        avaliacao.setNotaConservacao(4);
        avaliacao.setNotaFidelidade(5);
        avaliacao.setNotaPontualidade(5);
        avaliacao.setNotaSuporte(4);
        avaliacao.setMediaFinal(4.6);
        avaliacao.setComentario("Entrega pontual na fazenda.");
        avaliacao.setRecomenda(true);
        avaliacao.setDataAvaliacao(DATA_FIXA);

        // Assert (Consultas e Predicativo)
        assertEquals("av-1", avaliacao.getId());
        assertEquals("loc-10", avaliacao.getLocacaoId());
        assertEquals(5, avaliacao.getNotaDesempenho());
        assertEquals(4, avaliacao.getNotaConservacao());
        assertEquals(4.6, avaliacao.getMediaFinal());
        assertEquals(DATA_FIXA, avaliacao.getDataAvaliacao());
        assertTrue(avaliacao.isRecomenda());
        assertTrue(avaliacao.toString().contains("loc-10"));
        assertTrue(avaliacao.toString().contains("4.6"));
        assertTrue(avaliacao.toString().contains("recomenda"));
    }

    // Categorias: Inicializacao (construtor completo e de copia) + Computacao (clone, hashCode, compareTo) + Predicativo (equals)
    @Test
    void categoriasDeComputacaoEPredicativosDevemManterConsistenciaDeCopiaEOrdenacao() throws Exception {
        // Arrange
        Avaliacao notaBaixa = new Avaliacao("loc-1", "locador-1", "locatario-1", "maq-1",
                2, 2, 2, 2, 2, 2.0, "Atrasou entrega", false);
        notaBaixa.setDataAvaliacao(DATA_FIXA);

        Avaliacao notaAlta = new Avaliacao("loc-2", "locador-1", "locatario-2", "maq-1",
                5, 5, 5, 5, 5, 5.0, "Excelente", true);
        notaAlta.setDataAvaliacao(DATA_FIXA);

        // Action
        Avaliacao copia = new Avaliacao(notaAlta);
        Avaliacao clone = (Avaliacao) notaAlta.clone();

        // Assert (Predicativos e Computacao)
        assertNotSame(notaAlta, copia);
        assertNotSame(notaAlta, clone);
        assertTrue(notaAlta.equals(copia));
        assertTrue(notaAlta.equals(clone));
        assertEquals(notaAlta.hashCode(), copia.hashCode());
        assertEquals(notaAlta.hashCode(), clone.hashCode());
        assertFalse(notaBaixa.isRecomenda());
        assertTrue(notaBaixa.toString().contains("nao recomenda"));
        assertTrue(notaBaixa.compareTo(notaAlta) < 0);
        assertTrue(notaAlta.compareTo(notaBaixa) > 0);
        assertEquals(0, notaAlta.compareTo(copia));
        assertNotEquals(notaBaixa, notaAlta);
    }

    // Anomalias na inicializacao e nos limites de notas da entidade
    @Test
    void inicializacaoNulaLancaExcecaoEConstrutorAceitaNotasForaDoIntervaloSemValidacao() {
        // Arrange & Action & Assert (construtor de copia com modelo nulo)
        Exception erroCopia = assertThrows(Exception.class, () -> new Avaliacao((Avaliacao) null));
        assertEquals("Modelo ausente", erroCopia.getMessage());

        // Arrange & Action (anomalia: entidade aceita notas 0 e 6 fora do intervalo 1..5)
        Avaliacao inconsistente = new Avaliacao("loc-99", "locador-1", "locatario-1", "maq-1",
                0, 6, 1, 1, 1, 9.5, "Sem validacao na entidade", true);

        // Assert: documenta que a invariante de notas 1..5 nao e protegida na entidade
        assertNotNull(inconsistente.getDataAvaliacao());
        assertEquals(0, inconsistente.getNotaDesempenho());
        assertEquals(6, inconsistente.getNotaConservacao());
        assertEquals(9.5, inconsistente.getMediaFinal());
        assertThrows(NullPointerException.class, () -> inconsistente.compareTo(null));
    }
}
