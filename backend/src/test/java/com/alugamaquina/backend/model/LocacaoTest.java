/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Testes JUnit 5 dos métodos canônicos da entidade Locacao.
 */

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class LocacaoTest {

    private static final LocalDateTime DATA_FIXA = LocalDateTime.of(2026, 11, 1, 10, 0);

    // Cria uma locacao de exemplo com data de solicitacao fixa
    private Locacao criarLocacao(String id, LocalDate inicio) {
        Locacao locacao = new Locacao("locador1", "locatario1", "maquina1",
                inicio, inicio.plusDays(4),
                new BigDecimal("850.00"), new BigDecimal("475.00"), new BigDecimal("4725.00"));
        locacao.setId(id);
        locacao.setDataSolicitacao(DATA_FIXA);
        return locacao;
    }

    @Test
    void novaLocacaoDeveNascerPendente() {
        Locacao locacao = new Locacao();

        assertEquals(StatusLocacao.PENDENTE, locacao.getStatus());
        assertEquals(StatusLocacao.PENDENTE, criarLocacao("loc1", LocalDate.of(2026, 11, 10)).getStatus());
    }

    @Test
    void valoresDevemFicarComDuasCasasDecimais() {
        Locacao locacao = new Locacao("l", "t", "m", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10),
                new BigDecimal("850"), new BigDecimal("0"), new BigDecimal("850.5"));

        assertEquals(new BigDecimal("850.00"), locacao.getValorDiaria());
        assertEquals(new BigDecimal("0.00"), locacao.getValorFrete());
        assertEquals(new BigDecimal("850.50"), locacao.getValorTotal());
    }

    @Test
    void equalsDeveSerVerdadeiroParaMesmosDados() {
        assertEquals(criarLocacao("loc1", LocalDate.of(2026, 11, 10)),
                criarLocacao("loc1", LocalDate.of(2026, 11, 10)));
    }

    @Test
    void equalsDeveIgnorarDiferencaDeCasasDecimaisNosValores() {
        Locacao a = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        Locacao b = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        b.setValorDiaria(new BigDecimal("850"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsDeveSerFalsoParaDadosDiferentesNuloOuOutraClasse() {
        Locacao locacao = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        Locacao outroStatus = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        outroStatus.setStatus(StatusLocacao.ACEITO);

        assertFalse(locacao.equals(criarLocacao("loc2", LocalDate.of(2026, 11, 10))));
        assertFalse(locacao.equals(criarLocacao("loc1", LocalDate.of(2026, 11, 11))));
        assertFalse(locacao.equals(outroStatus));
        assertFalse(locacao.equals(null));
        assertFalse(locacao.equals("loc1"));
        assertTrue(locacao.equals(locacao));
    }

    @Test
    void hashCodeDeveSerIgualParaObjetosIguaisEPositivo() {
        Locacao a = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        Locacao b = criarLocacao("loc1", LocalDate.of(2026, 11, 10));

        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.hashCode() >= 0);
    }

    @Test
    void compareToDeveOrdenarPelaDataDeInicio() {
        Locacao cedo = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        Locacao tarde = criarLocacao("loc2", LocalDate.of(2026, 11, 20));

        assertTrue(cedo.compareTo(tarde) < 0);
        assertTrue(tarde.compareTo(cedo) > 0);
        assertEquals(0, cedo.compareTo(criarLocacao("loc3", LocalDate.of(2026, 11, 10))));
    }

    @Test
    void compareToDeveTratarDataDeInicioNula() {
        Locacao comData = criarLocacao("loc1", LocalDate.of(2026, 11, 10));
        Locacao semData = new Locacao();

        assertTrue(semData.compareTo(comData) < 0);
        assertTrue(comData.compareTo(semData) > 0);
        assertEquals(0, semData.compareTo(new Locacao()));
    }

    @Test
    void toStringDeveInformarMaquinaPeriodoTotalEStatus() {
        String texto = criarLocacao("loc1", LocalDate.of(2026, 11, 10)).toString();

        assertTrue(texto.contains("loc1"));
        assertTrue(texto.contains("maquina1"));
        assertTrue(texto.contains("2026-11-10"));
        assertTrue(texto.contains("4725.00"));
        assertTrue(texto.contains("PENDENTE"));
    }

    @Test
    void construtorDeCopiaEcloneDevemGerarObjetosIguaisEIndependentes() throws Exception {
        Locacao original = criarLocacao("loc1", LocalDate.of(2026, 11, 10));

        Locacao copia = new Locacao(original);
        Locacao clone = (Locacao) original.clone();

        assertEquals(original, copia);
        assertEquals(original, clone);
        assertNotSame(original, copia);
        assertNotSame(original, clone);

        clone.setStatus(StatusLocacao.ACEITO);
        assertEquals(StatusLocacao.PENDENTE, original.getStatus());
    }

    @Test
    void construtorDeCopiaComModeloNuloDeveLancarExcecao() {
        Exception erro = assertThrows(Exception.class, () -> new Locacao((Locacao) null));

        assertEquals("Modelo ausente", erro.getMessage());
    }
}
