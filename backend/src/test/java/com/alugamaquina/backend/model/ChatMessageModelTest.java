/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Testes unitarios JUnit 5 dos metodos canonicos da entidade ChatMessage (Issue #46)
*/
package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ChatMessageModelTest {

    private ChatMessage criarMensagem(String id, String remetente, String destinatario, String conteudo, LocalDateTime timestamp) {
        ChatMessage msg = new ChatMessage();
        msg.setId(id);
        msg.setRemetente(remetente);
        msg.setDestinatario(destinatario);
        msg.setConteudo(conteudo);
        msg.setHorario("10:15");
        msg.setTimestamp(timestamp);
        return msg;
    }

    @Test
    void construtorComCamposDevePreencherDadosETimestamp() {
        // Arrange + Act
        ChatMessage msg = new ChatMessage("Antonio", "Jose Carlos", "Bom dia!", "09:30");

        // Assert
        assertEquals("Antonio", msg.getRemetente());
        assertEquals("Jose Carlos", msg.getDestinatario());
        assertEquals("Bom dia!", msg.getConteudo());
        assertEquals("09:30", msg.getHorario());
        assertNotNull(msg.getTimestamp());
    }

    @Test
    void equalsDeveSerVerdadeiroParaMensagensComMesmosDados() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 10, 15);
        ChatMessage a = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);
        ChatMessage b = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);

        // Act
        boolean resultado = a.equals(b);

        // Assert
        assertTrue(resultado);
        assertEquals(a, b);
    }

    @Test
    void equalsDeveSerFalsoParaDadosDiferentesNuloOuOutraClasse() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 10, 15);
        ChatMessage a = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);
        ChatMessage diferente = criarMensagem("msg-2", "Antonio", "Jose Carlos", "Ola!", agora);

        // Act + Assert
        assertTrue(a.equals(a));
        assertFalse(a.equals(diferente));
        assertFalse(a.equals(null));
        assertFalse(a.equals("texto"));
    }

    @Test
    void hashCodeDeveSerIgualParaObjetosIguaisEPositivo() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 10, 15);
        ChatMessage a = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);
        ChatMessage b = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);

        // Act
        int hashA = a.hashCode();
        int hashB = b.hashCode();

        // Assert
        assertEquals(hashA, hashB);
        assertTrue(hashA >= 0);
    }

    @Test
    void compareToDeveOrdenarPorTimestampEDepoisPorId() {
        // Arrange
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 19, 10, 14);
        LocalDateTime t2 = LocalDateTime.of(2026, 11, 19, 10, 20);
        ChatMessage primeira = criarMensagem("msg-1", "Jose", "Antonio", "Oi", t1);
        ChatMessage segunda = criarMensagem("msg-2", "Antonio", "Jose", "Tudo bem?", t2);
        ChatMessage mesmoHorarioOutroId = criarMensagem("msg-3", "Jose", "Antonio", "Sim!", t1);

        // Act + Assert
        assertTrue(primeira.compareTo(segunda) < 0);
        assertTrue(segunda.compareTo(primeira) > 0);
        assertTrue(primeira.compareTo(mesmoHorarioOutroId) < 0);
        assertEquals(0, primeira.compareTo(criarMensagem("msg-1", "Jose", "Antonio", "Oi", t1)));
    }

    @Test
    void toStringDeveConterInformacoesPrincipaisDaMensagem() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 10, 15);
        ChatMessage msg = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);

        // Act
        String texto = msg.toString();

        // Assert
        assertTrue(texto.contains("msg-1"));
        assertTrue(texto.contains("Antonio"));
        assertTrue(texto.contains("Jose Carlos"));
        assertTrue(texto.contains("10:15"));
    }

    @Test
    void construtorDeCopiaDeveCopiarTodosOsCampos() throws Exception {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 10, 15);
        ChatMessage original = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);

        // Act
        ChatMessage copia = new ChatMessage(original);

        // Assert
        assertEquals(original, copia);
        assertNotSame(original, copia);
    }

    @Test
    void construtorDeCopiaComModeloNuloDeveLancarExcecao() {
        // Arrange
        ChatMessage nulo = null;

        // Act
        Exception erro = assertThrows(Exception.class, () -> new ChatMessage(nulo));

        // Assert
        assertEquals("Modelo ausente", erro.getMessage());
    }

    @Test
    void cloneDeveGerarCopiaIndependenteDoOriginal() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 10, 15);
        ChatMessage original = criarMensagem("msg-1", "Antonio", "Jose Carlos", "Ola!", agora);

        // Act
        ChatMessage clone = (ChatMessage) original.clone();
        clone.setConteudo("Alterado");

        // Assert
        assertNotSame(original, clone);
        assertEquals("Ola!", original.getConteudo());
        assertEquals("Alterado", clone.getConteudo());
    }
}
