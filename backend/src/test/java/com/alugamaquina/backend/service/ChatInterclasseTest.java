/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Testes de integracao Interclasse (Controller -> Service -> Model -> Repository) do Chat (Issue #46)
*/
package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alugamaquina.backend.controller.ChatController;
import com.alugamaquina.backend.model.ChatMessage;
import com.alugamaquina.backend.repository.ChatMessageRepository;

class ChatInterclasseTest {

    private List<ChatMessage> bancoEmMemoria;
    private ChatMessageRepository repository;
    private ChatMessageService service;
    private ChatController controller;

    @BeforeEach
    void setUp() {
        bancoEmMemoria = new ArrayList<>();
        repository = mock(ChatMessageRepository.class);

        when(repository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            if (msg.getId() == null) {
                msg.setId("msg-" + (bancoEmMemoria.size() + 1));
            }
            bancoEmMemoria.add(msg);
            return msg;
        });

        when(repository.findByRemetenteAndDestinatarioOrderByTimestampAsc(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String rem = invocation.getArgument(0);
                    String dest = invocation.getArgument(1);
                    return bancoEmMemoria.stream()
                            .filter(m -> rem.equals(m.getRemetente()) && dest.equals(m.getDestinatario()))
                            .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                            .toList();
                });

        when(repository.findAllByOrderByTimestampAsc())
                .thenAnswer(invocation -> bancoEmMemoria.stream()
                        .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                        .toList());

        service = new ChatMessageService(repository);
        controller = new ChatController(service);
    }

    @Test
    void fluxoCompletoDeEnvioDeveValidarSanitizarEPersistirNoRepositorio() throws Exception {
        // Arrange
        ChatMessage entrada = new ChatMessage(
                "  Antonio  ",
                "  Jose Carlos  ",
                "<b>Ola!</b> Preciso da betoneira.",
                "14:00");

        // Act
        ChatMessage salva = controller.enviarMensagem(entrada);

        // Assert
        assertNotNull(salva.getId());
        assertNotNull(salva.getTimestamp());
        assertEquals("Antonio", salva.getRemetente());
        assertEquals("Jose Carlos", salva.getDestinatario());
        assertEquals("&lt;b&gt;Ola!&lt;/b&gt; Preciso da betoneira.", salva.getConteudo());
        assertEquals(1, bancoEmMemoria.size());
        verify(repository).save(any(ChatMessage.class));
    }

    @Test
    void fluxoDeEnvioEHistoricoFiltradoDeveRetornarApenasAConversaSolicitada() throws Exception {
        // Arrange
        controller.enviarMensagem(new ChatMessage("Antonio", "Jose Carlos", "Primeira mensagem", "09:00"));
        controller.enviarMensagem(new ChatMessage("Jose Carlos", "Maria", "Outra conversa", "09:05"));

        // Act
        List<ChatMessage> conversa = controller.historico("Antonio", "Jose Carlos");

        // Assert
        assertEquals(1, conversa.size());
        assertEquals("Antonio", conversa.get(0).getRemetente());
        assertEquals("Jose Carlos", conversa.get(0).getDestinatario());
        assertEquals("Primeira mensagem", conversa.get(0).getConteudo());
        verify(repository).findByRemetenteAndDestinatarioOrderByTimestampAsc("Antonio", "Jose Carlos");
    }

    @Test
    void fluxoDeHistoricoSemFiltroDeveRetornarTodasAsMensagensPersistidas() throws Exception {
        // Arrange
        controller.enviarMensagem(new ChatMessage("Antonio", "Jose Carlos", "Ola!", "10:00"));
        controller.enviarMensagem(new ChatMessage("Jose Carlos", "Antonio", "Tudo certo!", "10:01"));

        // Act
        List<ChatMessage> todas = controller.historico(null, null);

        // Assert
        assertEquals(2, todas.size());
        verify(repository).findAllByOrderByTimestampAsc();
    }

    @Test
    void fluxoComRemetenteIgualAoDestinatarioDeveFalharNoServiceENaoPersistir() {
        // Arrange
        ChatMessage invalida = new ChatMessage();
        invalida.setRemetente("Antonio");
        invalida.setDestinatario("antonio");
        invalida.setConteudo("Mensagem para mim mesmo");
        invalida.setHorario("11:00");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> controller.enviarMensagem(invalida));
        assertEquals(0, bancoEmMemoria.size());
        verify(repository, never()).save(any());
    }

    @Test
    void fluxoComConteudoAcimaDoLimiteDeveFalharNoServiceENaoPersistir() {
        // Arrange
        ChatMessage invalida = new ChatMessage();
        invalida.setRemetente("Antonio");
        invalida.setDestinatario("Jose Carlos");
        invalida.setConteudo("x".repeat(ChatMessage.TAMANHO_MAXIMO_CONTEUDO + 1));
        invalida.setHorario("11:30");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> controller.enviarMensagem(invalida));
        assertEquals(0, bancoEmMemoria.size());
        verify(repository, never()).save(any());
    }

    @Test
    void fluxoDePersistenciaDeveIsolarObjetoRecebidoViaClone() throws Exception {
        // Arrange
        ChatMessage entrada = new ChatMessage("Antonio", "Jose Carlos", "Valor <b>100</b>", "15:10");

        // Act
        ChatMessage salva = controller.enviarMensagem(entrada);
        entrada.setConteudo("Alterado localmente");

        // Assert
        assertNotSame(entrada, salva);
        assertEquals("Alterado localmente", entrada.getConteudo());
        assertEquals("Valor &lt;b&gt;100&lt;/b&gt;", bancoEmMemoria.get(0).getConteudo());
    }
}
