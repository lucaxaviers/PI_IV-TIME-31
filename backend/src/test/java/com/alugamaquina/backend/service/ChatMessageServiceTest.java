/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Testes unitarios JUnit 5 de ChatMessageService com AAA, BVA e Mockito (Issue #46)

  Fronteiras testadas (conteudo):
    vazio / nulo / espacos -> invalido (IllegalArgumentException)
    1 caractere            -> valido (fronteira minima)
    1000 caracteres        -> valido (fronteira maxima)
    1001 caracteres        -> invalido (excede limite)
*/
package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.alugamaquina.backend.model.ChatMessage;
import com.alugamaquina.backend.repository.ChatMessageRepository;

class ChatMessageServiceTest {

    private ChatMessageRepository repository;
    private ChatMessageService service;

    @BeforeEach
    void setUp() {
        repository = mock(ChatMessageRepository.class);
        service = new ChatMessageService(repository);
        when(repository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ChatMessage criarMensagem(String remetente, String destinatario, String conteudo) {
        ChatMessage msg = new ChatMessage();
        msg.setRemetente(remetente);
        msg.setDestinatario(destinatario);
        msg.setConteudo(conteudo);
        msg.setHorario("10:30");
        return msg;
    }

    // ---------- Validacoes e BVA de conteudo ----------

    @Test
    void mensagemNulaDeveLancarExcecao() {
        // Arrange
        ChatMessage mensagem = null;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.salvarMensagem(mensagem));
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void conteudoVazioOuNuloDeveLancarExcecao(String conteudoInvalido) {
        // Arrange
        ChatMessage mensagem = criarMensagem("Antonio", "Jose Carlos", conteudoInvalido);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.salvarMensagem(mensagem));
        verify(repository, never()).save(any());
    }

    @Test
    void conteudoComUmCaractereDeveSerAceitoFronteiraMinima() {
        // Arrange
        ChatMessage mensagem = criarMensagem("Antonio", "Jose Carlos", "A");

        // Act
        ChatMessage salva = service.salvarMensagem(mensagem);

        // Assert
        assertEquals("A", salva.getConteudo());
        assertNotNull(salva.getTimestamp());
        verify(repository).save(any(ChatMessage.class));
    }

    @Test
    void conteudoComMilCaracteresDeveSerAceitoFronteiraMaxima() {
        // Arrange
        String textoMaximo = "a".repeat(ChatMessageService.TAMANHO_MAXIMO);
        ChatMessage mensagem = criarMensagem("Antonio", "Jose Carlos", textoMaximo);

        // Act
        ChatMessage salva = service.salvarMensagem(mensagem);

        // Assert
        assertEquals(1000, salva.getConteudo().length());
        verify(repository).save(any(ChatMessage.class));
    }

    @Test
    void conteudoComMilEUmCaracteresDeveLancarExcecao() {
        // Arrange
        String textoAcima = "a".repeat(ChatMessageService.TAMANHO_MAXIMO + 1);
        ChatMessage mensagem = criarMensagem("Antonio", "Jose Carlos", textoAcima);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.salvarMensagem(mensagem));
        verify(repository, never()).save(any());
    }

    // ---------- Remetente, Destinatario e Sanitizacao ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void remetenteInvalidoDeveLancarExcecao(String remetenteInvalido) {
        // Arrange
        ChatMessage mensagem = criarMensagem(remetenteInvalido, "Jose Carlos", "Bom dia!");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.salvarMensagem(mensagem));
        verify(repository, never()).save(any());
    }

    @Test
    void remetenteIgualAoDestinatarioDeveLancarExcecao() {
        // Arrange
        ChatMessage mensagem = criarMensagem("Antonio", "antonio", "Teste");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.salvarMensagem(mensagem));
        verify(repository, never()).save(any());
    }

    @Test
    void deveSanitizarTagsHtmlAntesDeSalvarNoBanco() {
        // Arrange
        ChatMessage mensagem = criarMensagem("Antonio", "Jose Carlos", "<script>alert('xss')</script>");

        // Act
        ChatMessage salva = service.salvarMensagem(mensagem);

        // Assert
        assertEquals("&lt;script&gt;alert('xss')&lt;/script&gt;", salva.getConteudo());
        verify(repository).save(any(ChatMessage.class));
    }

    // ---------- Historico ----------

    @Test
    void listarHistoricoComRemetenteEDestinatarioDeveFiltrarConversa() {
        // Arrange
        ChatMessage msg = criarMensagem("Antonio", "Jose Carlos", "Tudo certo?");
        when(repository.findByRemetenteAndDestinatarioOrderByTimestampAsc("Antonio", "Jose Carlos"))
                .thenReturn(List.of(msg));

        // Act
        List<ChatMessage> resultado = service.listarHistorico("Antonio", "Jose Carlos");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Tudo certo?", resultado.get(0).getConteudo());
        verify(repository).findByRemetenteAndDestinatarioOrderByTimestampAsc("Antonio", "Jose Carlos");
    }

    @Test
    void listarHistoricoSemFiltroDeveRetornarTodasAsMensagensOrdenadas() {
        // Arrange
        ChatMessage m1 = criarMensagem("Jose Carlos", "Antonio", "Ola");
        ChatMessage m2 = criarMensagem("Antonio", "Jose Carlos", "Bom dia");
        when(repository.findAllByOrderByTimestampAsc()).thenReturn(List.of(m1, m2));

        // Act
        List<ChatMessage> resultado = service.listarHistorico(null, null);

        // Assert
        assertEquals(2, resultado.size());
        verify(repository).findAllByOrderByTimestampAsc();
    }
}
