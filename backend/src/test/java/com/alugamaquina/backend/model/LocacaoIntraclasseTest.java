/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes OO Intraclasse (Particionamento por Estados) da entidade Locacao
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Testes OO Intraclasse da classe de dominio Locacao usando Particionamento por Estados
 * (StatusLocacao: PENDENTE, ACEITO, RECUSADO, CANCELADO, FINALIZADO)
 * no padrao AAA (Arrange, Action, Assert).
 */
class LocacaoIntraclasseTest {

    private static final LocalDateTime DATA_FIXA = LocalDateTime.of(2026, 11, 15, 9, 0);

    private Locacao novaPendente() {
        Locacao loc = new Locacao(
                "locador-1", "locatario-2", "maq-1",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24),
                new BigDecimal("850.00"), new BigDecimal("475.00"), new BigDecimal("4725.00")
        );
        loc.setId("loc-1");
        loc.setDataSolicitacao(DATA_FIXA);
        return loc;
    }

    // Ciclo de estados validos 1: PENDENTE -> ACEITO -> FINALIZADO
    @Test
    void cicloDeEstadosPendenteParaAceitoEFinalizadoDeveReterEstadoAtualizado() {
        // Arrange: toda nova instancia nasce no estado PENDENTE
        Locacao locacao = novaPendente();
        assertEquals(StatusLocacao.PENDENTE, locacao.getStatus());

        // Action 1: transicao PENDENTE -> ACEITO
        locacao.setStatus(StatusLocacao.ACEITO);
        StatusLocacao estadoAposAceite = locacao.getStatus();

        // Action 2: transicao ACEITO -> FINALIZADO
        locacao.setStatus(StatusLocacao.FINALIZADO);

        // Assert
        assertEquals(StatusLocacao.ACEITO, estadoAposAceite);
        assertEquals(StatusLocacao.FINALIZADO, locacao.getStatus());
        assertTrue(locacao.toString().contains("FINALIZADO"));
    }

    // Ciclo de estados validos 2: PENDENTE -> RECUSADO (com motivo) e PENDENTE -> CANCELADO
    @Test
    void transicoesParaRecusadoECanceladoDevemRefletirEmStatusMotivoEqualsEHashCode() throws Exception {
        // Arrange
        Locacao recusada = novaPendente();
        Locacao cancelada = new Locacao(recusada);

        // Action
        recusada.setStatus(StatusLocacao.RECUSADO);
        recusada.setMotivoRecusa("Maquina em manutencao preventiva.");
        cancelada.setStatus(StatusLocacao.CANCELADO);

        // Assert
        assertEquals(StatusLocacao.RECUSADO, recusada.getStatus());
        assertEquals("Maquina em manutencao preventiva.", recusada.getMotivoRecusa());
        assertEquals(StatusLocacao.CANCELADO, cancelada.getStatus());
        assertNotEquals(recusada, cancelada);
        assertNotEquals(recusada.hashCode(), cancelada.hashCode());
        assertTrue(recusada.toString().contains("RECUSADO"));
    }

    // Anomalias na maquina de estados da entidade: setStatus aceita transicao regressiva/nula sem guarda
    @Test
    void setStatusNaEntidadeAceitaTransicaoInvalidaENulaSemValidacaoDeDominio() {
        // Arrange
        Locacao locacao = novaPendente();
        locacao.setStatus(StatusLocacao.RECUSADO);

        // Action: entidade permite regressao RECUSADO -> ACEITO e ate status null
        Locacao cloneAntes = (Locacao) locacao.clone();
        locacao.setStatus(StatusLocacao.ACEITO);
        StatusLocacao aposRegressao = locacao.getStatus();
        locacao.setStatus(null);

        // Assert: documenta que a maquina de estados so e protegida no LocacaoService
        assertNotSame(locacao, cloneAntes);
        assertEquals(StatusLocacao.RECUSADO, cloneAntes.getStatus());
        assertEquals(StatusLocacao.ACEITO, aposRegressao);
        assertEquals(null, locacao.getStatus());
        assertThrows(NullPointerException.class, () -> locacao.compareTo(null));
    }
}
