/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Testes JUnit 5 da consulta de locações no LocacaoService (buscar por id e listar por
 *            usuário) e dos campos novos do LocacaoResponse, no padrão AAA.
 *
 * Partições testadas:
 *   - Id da locação:    existente (válido) | inexistente (404) | nulo, vazio ou só espaços (inválido)
 *   - Locações do usuário (BVA): 0 (lista vazia) | 1 | várias, na ordem do repositório (mais nova primeiro)
 *   - Papel do usuário: locador | locatário | os dois
 *   - Campos novos:     máquina/usuário achados | não achados (nulo) | sem repositórios (nulo)
 */

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.alugamaquina.backend.dto.LocacaoResponse;
import com.alugamaquina.backend.dto.SolicitacaoLocacaoRequest;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.LocacaoRepository;
import com.alugamaquina.backend.repository.MaquinaRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

class LocacaoConsultaServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 10);

    private LocacaoRepository locacaoRepository;
    private MaquinaRepository maquinaRepository;
    private UsuarioRepository usuarioRepository;
    private LocacaoService service;

    @BeforeEach
    // Cria o service com os tres repositorios falsos (mock)
    void setUp() {
        locacaoRepository = mock(LocacaoRepository.class);
        maquinaRepository = mock(MaquinaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new LocacaoService(locacaoRepository, maquinaRepository, usuarioRepository);
    }

    // ---------- Apoio dos testes ----------

    private Locacao locacao(String id, String locadorId, String locatarioId, String maquinaId) {
        Locacao locacao = new Locacao(locadorId, locatarioId, maquinaId,
                INICIO, INICIO.plusDays(4),
                new BigDecimal("850.00"), new BigDecimal("475.00"), new BigDecimal("4725.00"));
        locacao.setId(id);
        return locacao;
    }

    private void simularMaquina(String id, String titulo) {
        Maquina maquina = mock(Maquina.class);
        when(maquina.getTituloAnuncio()).thenReturn(titulo);
        when(maquinaRepository.findById(id)).thenReturn(Optional.of(maquina));
    }

    private void simularUsuario(String id, String nome) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getNome()).thenReturn(nome);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
    }

    // ---------- Buscar por id ----------

    @Test
    // CT-LOC-01 | id existente: devolve a locacao com os campos novos preenchidos
    void deveBuscarLocacaoPorIdComTituloENomeDoLocador() {
        // Arrange
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao("loc-1", "locador-1", "locatario-1", "maq-1")));
        simularMaquina("maq-1", "Trator John Deere 6115J");
        simularUsuario("locador-1", "Maria Souza");

        // Act
        LocacaoResponse response = service.buscarPorId("loc-1");

        // Assert
        assertTrue(response.isSucesso());
        assertEquals("loc-1", response.getId());
        assertEquals("maq-1", response.getMaquinaId());
        assertEquals("locador-1", response.getLocadorId());
        assertEquals("locatario-1", response.getLocatarioId());
        assertEquals("Trator John Deere 6115J", response.getTituloMaquina());
        assertEquals("Maria Souza", response.getNomeLocador());
        assertEquals(StatusLocacao.PENDENTE, response.getStatus());
        assertEquals(INICIO, response.getDataInicio());
        assertEquals(INICIO.plusDays(4), response.getDataFim());
        assertEquals(5, response.getDiarias());
        assertEquals(new BigDecimal("4725.00"), response.getValorTotal());
    }

    @Test
    // CT-LOC-02 | id inexistente: lanca LocacaoNaoEncontradaException (vira 404 no controller)
    void deveLancarNaoEncontradaQuandoIdNaoExiste() {
        // Arrange
        when(locacaoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        // Act + Assert
        LocacaoNaoEncontradaException erro = assertThrows(LocacaoNaoEncontradaException.class,
                () -> service.buscarPorId("nao-existe"));
        assertTrue(erro.getMessage().contains("nao-existe"));
    }

    @ParameterizedTest(name = "CT-LOC-03 id=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    // CT-LOC-03 | id nulo, vazio ou so espacos: dado invalido, nem consulta o banco
    void deveRejeitarIdInvalidoAoBuscar(String idInvalido) {
        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(idInvalido));
        verify(locacaoRepository, never()).findById(any());
    }

    @Test
    // CT-LOC-04 | maquina e usuario nao achados: titulo e nome ficam nulos, sem erro
    void deveDeixarTituloENomeNulosQuandoNaoEncontrados() {
        // Arrange
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao("loc-1", "locador-1", "locatario-1", "maq-1")));
        when(maquinaRepository.findById("maq-1")).thenReturn(Optional.empty());
        when(usuarioRepository.findById("locador-1")).thenReturn(Optional.empty());

        // Act
        LocacaoResponse response = service.buscarPorId("loc-1");

        // Assert
        assertNull(response.getTituloMaquina());
        assertNull(response.getNomeLocador());
        assertEquals("locador-1", response.getLocadorId());
        assertEquals("locatario-1", response.getLocatarioId());
    }

    @Test
    // CT-LOC-05 | construtor antigo (so LocacaoRepository): titulo e nome nulos, ids preenchidos
    void deveFuncionarSemRepositoriosDeMaquinaEUsuario() {
        // Arrange
        LocacaoService serviceSimples = new LocacaoService(locacaoRepository);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao("loc-1", "locador-1", "locatario-1", "maq-1")));

        // Act
        LocacaoResponse response = serviceSimples.buscarPorId("loc-1");

        // Assert
        assertNull(response.getTituloMaquina());
        assertNull(response.getNomeLocador());
        assertEquals("locador-1", response.getLocadorId());
    }

    @Test
    // CT-LOC-06 | id com espacos nas pontas e aparado antes de consultar
    void deveAparaOIdAntesDeBuscar() {
        // Arrange
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao("loc-1", "locador-1", "locatario-1", "maq-1")));

        // Act
        LocacaoResponse response = service.buscarPorId("  loc-1  ");

        // Assert
        assertEquals("loc-1", response.getId());
    }

    // ---------- Listar por usuario ----------

    @Test
    // CT-LOC-07 | BVA: usuario com 0 locacoes devolve lista vazia
    void deveDevolverListaVaziaQuandoUsuarioNaoTemLocacoes() {
        // Arrange
        when(locacaoRepository.findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1")).thenReturn(List.of());

        // Act
        List<LocacaoResponse> lista = service.listarPorUsuario("u-1");

        // Assert
        assertTrue(lista.isEmpty());
    }

    @Test
    // CT-LOC-08 | BVA: usuario com 1 locacao devolve lista de 1 item
    void deveListarUmaLocacaoDoUsuario() {
        // Arrange
        when(locacaoRepository.findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1"))
                .thenReturn(List.of(locacao("loc-1", "u-1", "locatario-1", "maq-1")));
        simularMaquina("maq-1", "Retroescavadeira CAT 416");

        // Act
        List<LocacaoResponse> lista = service.listarPorUsuario("u-1");

        // Assert
        assertEquals(1, lista.size());
        assertEquals("loc-1", lista.get(0).getId());
        assertEquals("Retroescavadeira CAT 416", lista.get(0).getTituloMaquina());
    }

    @Test
    // CT-LOC-09 | varias locacoes: mantem a ordem do repositorio (mais nova primeiro)
    void deveListarVariasLocacoesNaOrdemDoRepositorio() {
        // Arrange: o repositorio ja devolve da mais nova (loc-3) para a mais antiga (loc-1)
        when(locacaoRepository.findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1"))
                .thenReturn(List.of(
                        locacao("loc-3", "u-1", "x", "maq-1"),
                        locacao("loc-2", "u-1", "y", "maq-2"),
                        locacao("loc-1", "z", "u-1", "maq-3")));

        // Act
        List<LocacaoResponse> lista = service.listarPorUsuario("u-1");

        // Assert
        assertEquals(3, lista.size());
        assertEquals("loc-3", lista.get(0).getId());
        assertEquals("loc-2", lista.get(1).getId());
        assertEquals("loc-1", lista.get(2).getId());
        verify(locacaoRepository).findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1");
    }

    @Test
    // CT-LOC-10 | usuario aparece como locador em uma e como locatario em outra: as duas vem na lista
    void deveListarLocacoesComoLocadorEComoLocatario() {
        // Arrange
        when(locacaoRepository.findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1"))
                .thenReturn(List.of(
                        locacao("loc-2", "u-1", "outro", "maq-1"),
                        locacao("loc-1", "outro", "u-1", "maq-2")));

        // Act
        List<LocacaoResponse> lista = service.listarPorUsuario("u-1");

        // Assert
        assertEquals("u-1", lista.get(0).getLocadorId());
        assertEquals("outro", lista.get(0).getLocatarioId());
        assertEquals("outro", lista.get(1).getLocadorId());
        assertEquals("u-1", lista.get(1).getLocatarioId());
    }

    @ParameterizedTest(name = "CT-LOC-11 usuarioId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    // CT-LOC-11 | usuarioId nulo, vazio ou so espacos: dado invalido, nem consulta o banco
    void deveRejeitarUsuarioIdInvalidoAoListar(String idInvalido) {
        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.listarPorUsuario(idInvalido));
        verify(locacaoRepository, never()).findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc(any(), any());
    }

    @Test
    // CT-LOC-12 | tres locacoes da mesma maquina e do mesmo locador: busca titulo e nome uma vez so
    void deveBuscarTituloENomeUmaVezPorMaquinaELocador() {
        // Arrange
        when(locacaoRepository.findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1"))
                .thenReturn(List.of(
                        locacao("loc-3", "u-1", "a", "maq-1"),
                        locacao("loc-2", "u-1", "b", "maq-1"),
                        locacao("loc-1", "u-1", "c", "maq-1")));
        simularMaquina("maq-1", "Trator John Deere 6115J");
        simularUsuario("u-1", "Maria Souza");

        // Act
        List<LocacaoResponse> lista = service.listarPorUsuario("u-1");

        // Assert
        assertEquals(3, lista.size());
        lista.forEach(r -> assertEquals("Trator John Deere 6115J", r.getTituloMaquina()));
        lista.forEach(r -> assertEquals("Maria Souza", r.getNomeLocador()));
        verify(maquinaRepository, times(1)).findById("maq-1");
        verify(usuarioRepository, times(1)).findById("u-1");
    }

    @Test
    // CT-LOC-13 | usuarioId com espacos nas pontas e aparado antes de consultar
    void deveAparaOUsuarioIdAntesDeListar() {
        // Arrange
        when(locacaoRepository.findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1")).thenReturn(List.of());

        // Act
        service.listarPorUsuario("  u-1 ");

        // Assert
        verify(locacaoRepository).findByLocadorIdOrLocatarioIdOrderByDataSolicitacaoDesc("u-1", "u-1");
    }

    // ---------- Campos novos nas demais respostas ----------

    @Test
    // CT-LOC-14 | solicitar tambem devolve locadorId, locatarioId, tituloMaquina e nomeLocador
    void deveDevolverCamposNovosNaSolicitacao() {
        // Arrange
        when(locacaoRepository.save(any(Locacao.class))).thenAnswer(invocacao -> {
            Locacao salva = invocacao.getArgument(0);
            salva.setId("loc-1");
            return salva;
        });
        simularMaquina("maq-1", "Trator John Deere 6115J");
        simularUsuario("locador-1", "Maria Souza");
        SolicitacaoLocacaoRequest request = new SolicitacaoLocacaoRequest("locador-1", "locatario-1", "maq-1",
                INICIO, INICIO.plusDays(4), new BigDecimal("850.00"), 25);

        // Act
        LocacaoResponse response = service.solicitar(request);

        // Assert
        assertEquals("locador-1", response.getLocadorId());
        assertEquals("locatario-1", response.getLocatarioId());
        assertEquals("Trator John Deere 6115J", response.getTituloMaquina());
        assertEquals("Maria Souza", response.getNomeLocador());
        assertEquals(new BigDecimal("4725.00"), response.getValorTotal());
    }

    @Test
    // CT-LOC-15 | aceitar tambem devolve os campos novos
    void deveDevolverCamposNovosAoAceitar() {
        // Arrange
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao("loc-1", "locador-1", "locatario-1", "maq-1")));
        when(locacaoRepository.save(any(Locacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        simularMaquina("maq-1", "Trator John Deere 6115J");
        simularUsuario("locador-1", "Maria Souza");

        // Act
        LocacaoResponse response = service.aceitar("loc-1");

        // Assert
        assertEquals(StatusLocacao.ACEITO, response.getStatus());
        assertEquals("Trator John Deere 6115J", response.getTituloMaquina());
        assertEquals("Maria Souza", response.getNomeLocador());
        assertEquals("locatario-1", response.getLocatarioId());
    }
}
