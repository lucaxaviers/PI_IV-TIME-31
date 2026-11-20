/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Testes unitarios Intraclasse (JUnit 5 + AAA + BVA) do modelo ChatMessage (Issue #46)
*/
package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ChatMessageIntraclasseTest {

    // ---------- 1. Inicializacao ----------

    @Test
    void construtorVazioDeveInicializarAtributosNulos() {
        // Arrange + Act
        ChatMessage msg = new ChatMessage();

        // Assert
        assertNull(msg.getId());
        assertNull(msg.getRemetente());
        assertNull(msg.getDestinatario());
        assertNull(msg.getConteudo());
        assertNull(msg.getHorario());
        assertNull(msg.getTimestamp());
    }

    @Test
    void construtorPrincipalDeveApararEspacosDeRemetenteDestinatarioEHorario() throws Exception {
        // Arrange + Act
        ChatMessage msg = new ChatMessage("  Antonio  ", "  Jose Carlos  ", "Preciso da betoneira", "  09:30  ");

        // Assert
        assertEquals("Antonio", msg.getRemetente());
        assertEquals("Jose Carlos", msg.getDestinatario());
        assertEquals("Preciso da betoneira", msg.getConteudo());
        assertEquals("09:30", msg.getHorario());
        assertNotNull(msg.getTimestamp());
    }

    @Test
    void construtorPrincipalDeveAceitarHorarioNulo() throws Exception {
        // Arrange + Act
        ChatMessage msg = new ChatMessage("Antonio", "Jose Carlos", "Mensagem sem horario", null);

        // Assert
        assertNull(msg.getHorario());
        assertEquals("Mensagem sem horario", msg.getConteudo());
        assertNotNull(msg.getTimestamp());
    }

    // ---------- 2. Setup (Setters) ----------

    @Test
    void settersDevemAtualizarTodosOsAtributosSemLancarExcecao() {
        // Arrange
        ChatMessage msg = new ChatMessage();
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 14, 30);

        // Act
        msg.setId("msg-001");
        msg.setRemetente("Antonio");
        msg.setDestinatario("Jose Carlos");
        msg.setConteudo("Entrega confirmada");
        msg.setHorario("14:30");
        msg.setTimestamp(agora);

        // Assert
        assertEquals("msg-001", msg.getId());
        assertEquals("Antonio", msg.getRemetente());
        assertEquals("Jose Carlos", msg.getDestinatario());
        assertEquals("Entrega confirmada", msg.getConteudo());
        assertEquals("14:30", msg.getHorario());
        assertEquals(agora, msg.getTimestamp());
    }

    // ---------- 3. Consulta (Getters e toString) ----------

    @Test
    void toStringDeveConterCamposPrincipaisDaMensagem() throws Exception {
        // Arrange
        ChatMessage msg = new ChatMessage("Antonio", "Jose Carlos", "Maquina pronta", "10:15");

        // Act
        String representacao = msg.toString();

        // Assert
        assertNotNull(representacao);
        assertFalse(representacao.isBlank());
        assertTrue(representacao.contains("Antonio"));
        assertTrue(representacao.contains("Jose Carlos"));
        assertTrue(representacao.contains("10:15"));
    }

    // ---------- 4. Predicativos (Validacoes estaticas e BVA) ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void isTextoValidoDeveRejeitarNuloVazioEEspacos(String textoInvalido) {
        // Arrange + Act
        boolean valido = ChatMessage.isTextoValido(textoInvalido);

        // Assert
        assertFalse(valido);
    }

    @Test
    void isTextoValidoDeveAceitarTextoPreenchido() {
        // Arrange + Act
        boolean valido = ChatMessage.isTextoValido("Antonio");

        // Assert
        assertTrue(valido);
    }

    @Test
    void isConteudoValidoDeveRespeitarFronteirasMinimaEMaxima() {
        // Arrange
        String vazio = "   ";
        String umCaractere = "A";
        String limiteMaximo = "a".repeat(ChatMessage.TAMANHO_MAXIMO_CONTEUDO);
        String acimaDoLimite = "a".repeat(ChatMessage.TAMANHO_MAXIMO_CONTEUDO + 1);

        // Act + Assert
        assertFalse(ChatMessage.isConteudoValido(null));
        assertFalse(ChatMessage.isConteudoValido(vazio));
        assertTrue(ChatMessage.isConteudoValido(umCaractere));
        assertTrue(ChatMessage.isConteudoValido(limiteMaximo));
        assertFalse(ChatMessage.isConteudoValido(acimaDoLimite));
    }

    @Test
    void isDestinatarioValidoDeveRejeitarInvalidoOuIgualAoRemetente() {
        // Arrange + Act + Assert
        assertFalse(ChatMessage.isDestinatarioValido("Antonio", null));
        assertFalse(ChatMessage.isDestinatarioValido("Antonio", ""));
        assertFalse(ChatMessage.isDestinatarioValido("Antonio", "   "));
        assertFalse(ChatMessage.isDestinatarioValido("Antonio", "antonio"));
        assertFalse(ChatMessage.isDestinatarioValido("  Antonio ", "ANTONIO"));
        assertTrue(ChatMessage.isDestinatarioValido("Antonio", "Jose Carlos"));
    }

    // ---------- 5. Anomalias no Construtor Principal ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void construtorPrincipalDeveRejeitarRemetenteInvalido(String remetenteInvalido) {
        // Arrange + Act + Assert
        assertThrows(
                Exception.class,
                () -> new ChatMessage(remetenteInvalido, "Jose Carlos", "Oi", "09:00"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Antonio", "  antonio  "})
    void construtorPrincipalDeveRejeitarDestinatarioNuloVazioOuIgualAoRemetente(String destinatarioInvalido) {
        // Arrange + Act + Assert
        assertThrows(
                Exception.class,
                () -> new ChatMessage("Antonio", destinatarioInvalido, "Oi", "09:00"));
    }

    @Test
    void construtorPrincipalDeveRejeitarConteudoNuloVazioOuAcimaDoLimite() {
        // Arrange
        String conteudoAcima = "x".repeat(ChatMessage.TAMANHO_MAXIMO_CONTEUDO + 1);

        // Act + Assert
        assertThrows(Exception.class, () -> new ChatMessage("Antonio", "Jose Carlos", null, "09:00"));
        assertThrows(Exception.class, () -> new ChatMessage("Antonio", "Jose Carlos", "   ", "09:00"));
        assertThrows(Exception.class, () -> new ChatMessage("Antonio", "Jose Carlos", conteudoAcima, "09:00"));
    }

    // ---------- 6. Computacao (Copia, Clone, Equals, HashCode, CompareTo) ----------

    @Test
    void construtorDeCopiaECloneDevemGerarCopiaIndependente() throws Exception {
        // Arrange
        ChatMessage original = new ChatMessage("Antonio", "Jose Carlos", "Bom dia!", "09:30");
        original.setId("msg-1");

        // Act
        ChatMessage copiaConstrutor = new ChatMessage(original);
        ChatMessage copiaClone = (ChatMessage) original.clone();
        copiaClone.setConteudo("Editado");

        // Assert
        assertNotSame(original, copiaConstrutor);
        assertEquals(original, copiaConstrutor);
        assertEquals(original.hashCode(), copiaConstrutor.hashCode());
        assertEquals("Bom dia!", original.getConteudo());
        assertNotEquals(original, copiaClone);
    }

    @Test
    void construtorDeCopiaComModeloNuloDeveLancarExcecao() {
        // Arrange
        ChatMessage nulo = null;

        // Act + Assert
        assertThrows(Exception.class, () -> new ChatMessage(nulo));
    }

    @Test
    void equalsEHashCodeDevemSeguirContrato() throws Exception {
        // Arrange
        ChatMessage m1 = new ChatMessage("Antonio", "Jose Carlos", "Teste", "11:00");
        ChatMessage m2 = new ChatMessage(m1);
        ChatMessage m3 = new ChatMessage("Antonio", "Jose Carlos", "Outro", "11:00");

        // Act + Assert
        assertTrue(m1.equals(m1));
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));
        assertEquals(m1.hashCode(), m2.hashCode());
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("string qualquer"));
        assertFalse(m1.equals(m3));
    }

    @Test
    void compareToDeveOrdenarMensagensConsistentemente() throws Exception {
        // Arrange
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 19, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 11, 19, 18, 0);

        ChatMessage m1 = new ChatMessage("Alice", "Bruno", "Abc", "08:00");
        m1.setId("1");
        m1.setTimestamp(t1);

        ChatMessage copia = new ChatMessage(m1);

        ChatMessage m2 = new ChatMessage("Zeca", "Yuri", "Zzz", "18:00");
        m2.setId("2");
        m2.setTimestamp(t2);

        // Act + Assert
        assertEquals(0, m1.compareTo(copia));
        assertTrue(m1.compareTo(m2) < 0);
        assertTrue(m2.compareTo(m1) > 0);
    }
}
