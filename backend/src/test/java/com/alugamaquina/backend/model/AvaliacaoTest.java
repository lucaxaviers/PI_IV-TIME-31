/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes unitarios JUnit 5 dos metodos canonicos da entidade Avaliacao (Issue #46)
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AvaliacaoTest {

    private static final LocalDateTime DATA_FIXA = LocalDateTime.of(2026, 11, 1, 10, 0);

    // Cria uma avaliacao de exemplo com data fixa
    private Avaliacao criarAvaliacao(String locacaoId, double media) {
        Avaliacao avaliacao = new Avaliacao(locacaoId, "locador1", "locatario1", "maquina1",
                5, 4, 5, 4, 5, media, "Bom", true);
        avaliacao.setDataAvaliacao(DATA_FIXA);
        return avaliacao;
    }

    @Test
    void equalsDeveSerVerdadeiroParaMesmosDados() {
        assertEquals(criarAvaliacao("loc1", 4.6), criarAvaliacao("loc1", 4.6));
    }

    @Test
    void equalsDeveSerFalsoParaDadosDiferentesNuloOuOutraClasse() {
        Avaliacao avaliacao = criarAvaliacao("loc1", 4.6);

        assertFalse(avaliacao.equals(criarAvaliacao("loc2", 4.6)));
        assertFalse(avaliacao.equals(criarAvaliacao("loc1", 3.0)));
        assertFalse(avaliacao.equals(null));
        assertFalse(avaliacao.equals("loc1"));
        assertTrue(avaliacao.equals(avaliacao));
    }

    @Test
    void hashCodeDeveSerIgualParaObjetosIguaisEPositivo() {
        Avaliacao a = criarAvaliacao("loc1", 4.6);
        Avaliacao b = criarAvaliacao("loc1", 4.6);

        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.hashCode() >= 0);
    }

    @Test
    void compareToDeveOrdenarPelaMediaFinal() {
        Avaliacao baixa = criarAvaliacao("loc1", 2.0);
        Avaliacao alta = criarAvaliacao("loc2", 4.8);

        assertTrue(baixa.compareTo(alta) < 0);
        assertTrue(alta.compareTo(baixa) > 0);
        assertEquals(0, baixa.compareTo(criarAvaliacao("loc3", 2.0)));
    }

    @Test
    void toStringDeveInformarLocacaoMediaERecomendacao() {
        String texto = criarAvaliacao("loc1", 4.6).toString();

        assertTrue(texto.contains("loc1"));
        assertTrue(texto.contains("4.6"));
        assertTrue(texto.contains("recomenda"));
    }

    @Test
    void construtorDeCopiaEcloneDevemGerarObjetosIguaisEIndependentes() throws Exception {
        Avaliacao original = criarAvaliacao("loc1", 4.6);

        Avaliacao copia = new Avaliacao(original);
        Avaliacao clone = (Avaliacao) original.clone();

        assertEquals(original, copia);
        assertEquals(original, clone);
        assertNotSame(original, copia);
        assertNotSame(original, clone);

        clone.setComentario("Alterado");
        assertEquals("Bom", original.getComentario());
    }

    @Test
    void construtorDeCopiaComModeloNuloDeveLancarExcecao() {
        Exception erro = assertThrows(Exception.class, () -> new Avaliacao((Avaliacao) null));

        assertEquals("Modelo ausente", erro.getMessage());
    }
}
