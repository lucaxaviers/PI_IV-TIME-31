/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes OO Interclasse (integracao e ponta a ponta) do fluxo do contrato digital
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.alugamaquina.backend.controller.ContratoController;
import com.alugamaquina.backend.dto.AssinarContratoRequest;
import com.alugamaquina.backend.dto.ContratoResponse;
import com.alugamaquina.backend.dto.GerarContratoRequest;
import com.alugamaquina.backend.exception.AssinaturaNaoPermitidaException;
import com.alugamaquina.backend.exception.ContratoConflitoException;
import com.alugamaquina.backend.exception.ContratoNaoEncontradoException;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.model.Contrato;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.ContratoRepository;
import com.alugamaquina.backend.repository.LocacaoRepository;
import com.alugamaquina.backend.repository.MaquinaRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

/**
 * Testes de integracao entre classes (Issue #44), no padrao AAA.
 *
 * Estrategia incremental (duas classes por vez):
 *   ContratoService -> GeradorPdfContrato   (o servico entrega o contrato certo ao gerador)
 *   ContratoController -> ContratoService   (o controller traduz resposta e erros em HTTP)
 *
 * Estrategia ponta a ponta (thread do pedido "gerar, assinar e baixar"):
 *   ContratoController -> ContratoService -> repositorios (simulados) -> Contrato -> GeradorPdfContrato
 *
 * Os repositorios sao simulados, mas guardam os contratos em memoria para o fluxo completo funcionar.
 */
class ContratoInterclasseTest {

    private Map<String, Contrato> bancoDeContratos;
    private ContratoRepository contratoRepository;
    private LocacaoRepository locacaoRepository;
    private UsuarioRepository usuarioRepository;
    private MaquinaRepository maquinaRepository;
    private ContratoService servico;
    private ContratoController controller;

    @BeforeEach
    void preparar() {
        bancoDeContratos = new HashMap<>();
        contratoRepository = mock(ContratoRepository.class);
        locacaoRepository = mock(LocacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        maquinaRepository = mock(MaquinaRepository.class);

        // Repositorio de contratos em memoria: guarda no save e acha no findById
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(chamada -> {
            Contrato contrato = chamada.getArgument(0);
            if (contrato.getId() == null) {
                contrato.setId("con" + (bancoDeContratos.size() + 1));
            }
            bancoDeContratos.put(contrato.getId(), contrato);
            return contrato;
        });
        when(contratoRepository.findById(anyString()))
                .thenAnswer(chamada -> Optional.ofNullable(bancoDeContratos.get(chamada.<String>getArgument(0))));
        when(contratoRepository.findByLocacaoId(anyString())).thenAnswer(chamada -> bancoDeContratos.values().stream()
                .filter(contrato -> contrato.getLocacaoId().equals(chamada.getArgument(0))).findFirst());
        when(contratoRepository.existsByLocacaoId(anyString())).thenAnswer(chamada -> bancoDeContratos.values()
                .stream().anyMatch(contrato -> contrato.getLocacaoId().equals(chamada.getArgument(0))));
        when(contratoRepository.count()).thenAnswer(chamada -> (long) bancoDeContratos.size());

        servico = new ContratoService(contratoRepository, locacaoRepository, usuarioRepository,
                maquinaRepository, new GeradorPdfContrato());
        controller = new ContratoController(servico);
    }

    // ---------- Massa de dados ----------

    private void cadastrarLocacao(StatusLocacao status) {
        Locacao locacao = new Locacao("locador1", "locatario1", "maq1",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14),
                new BigDecimal("850.00"), new BigDecimal("364.00"), new BigDecimal("4614.00"));
        locacao.setId("loc1");
        locacao.setStatus(status);
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.of(locacao));

        when(usuarioRepository.findById("locador1")).thenReturn(Optional.of(criarUsuario("locador1", "Carlos Eduardo Pereira")));
        when(usuarioRepository.findById("locatario1")).thenReturn(Optional.of(criarUsuario("locatario1", "Antonio Ferreira da Silva")));

        Maquina maquina = new Maquina();
        maquina.setId("maq1");
        maquina.setTituloAnuncio("Trator John Deere 6115J");
        when(maquinaRepository.findById("maq1")).thenReturn(Optional.of(maquina));
    }

    private Usuario criarUsuario(String id, String nome) {
        Usuario usuario = new Usuario(nome, "12345678901", "19999990000", id + "@teste.com",
                "Paulinia", "ambos", "hash");
        usuario.setId(id);
        return usuario;
    }

    private GerarContratoRequest pedidoDeGerar(String locacaoId) {
        GerarContratoRequest pedido = new GerarContratoRequest();
        pedido.setLocacaoId(locacaoId);
        return pedido;
    }

    private AssinarContratoRequest pedidoDeAssinar(String usuarioId) {
        AssinarContratoRequest pedido = new AssinarContratoRequest();
        pedido.setUsuarioId(usuarioId);
        return pedido;
    }

    // ---------- Integracao incremental: Servico -> Gerador de PDF ----------

    // CT-INT-01 | o servico entrega ao gerador o contrato com os nomes e valores certos
    @Test
    void testServicoEntregaAoGeradorOContratoComDadosReais() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        ContratoResponse gerado = servico.gerar("loc1");

        // Action
        byte[] pdf = servico.gerarPdf(gerado.getId());

        // Assert
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
        assertTrue(pdf.length > 500);
    }

    // CT-INT-02 | o PDF refletea a assinatura registrada pelo servico
    @Test
    void testPdfMudaDepoisQueOServicoRegistraUmaAssinatura() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        ContratoResponse gerado = servico.gerar("loc1");
        byte[] antes = servico.gerarPdf(gerado.getId());

        // Action
        servico.assinar(gerado.getId(), "locador1");
        byte[] depois = servico.gerarPdf(gerado.getId());

        // Assert
        assertFalse(java.util.Arrays.equals(antes, depois));
    }

    // ---------- Integracao incremental: Controller -> Servico ----------

    // CT-INT-03 | POST gerar responde 201 com o contrato
    @Test
    void testControllerGerarResponde201ComOContrato() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);

        // Action
        ResponseEntity<ContratoResponse> resposta = controller.gerar(pedidoDeGerar("loc1"));

        // Assert
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody().getId());
        assertEquals("Carlos Eduardo Pereira", resposta.getBody().getNomeLocador());
    }

    // CT-INT-04 | corpo ausente vira erro 400 pelo servico
    @Test
    void testControllerGerarSemCorpoDaErroDeArgumento() {
        // Arrange - pedido nulo

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> controller.gerar(null));

        // Assert
        assertEquals("Identificador da locacao e obrigatorio.", erro.getMessage());
    }

    // CT-INT-05 | PDF baixado pelo controller tem tipo e nome de arquivo corretos
    @Test
    void testControllerBaixarPdfDevolveArquivoComTipoENome() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        ContratoResponse gerado = controller.gerar(pedidoDeGerar("loc1")).getBody();

        // Action
        ResponseEntity<byte[]> resposta = controller.baixarPdf(gerado.getId());

        // Assert
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, resposta.getHeaders().getContentType());
        assertTrue(resposta.getHeaders().getFirst("Content-Disposition")
                .contains("contrato-" + gerado.getNumero() + ".pdf"));
    }

    // CT-INT-06 | os tratadores de erro do controller devolvem o status HTTP certo
    @Test
    void testControllerTraduzCadaExcecaoParaOStatusHttpCerto() {
        // Arrange - uma excecao de cada tipo

        // Action
        ResponseEntity<Map<String, Object>> naoEncontrado =
                controller.tratarNaoEncontrado(new ContratoNaoEncontradoException("x"));
        ResponseEntity<Map<String, Object>> locacaoNaoEncontrada =
                controller.tratarNaoEncontrado(new LocacaoNaoEncontradaException("x"));
        ResponseEntity<Map<String, Object>> conflito =
                controller.tratarConflito(new ContratoConflitoException("x"));
        ResponseEntity<Map<String, Object>> proibido =
                controller.tratarAssinaturaNaoPermitida(new AssinaturaNaoPermitidaException("x"));
        ResponseEntity<Map<String, Object>> invalido =
                controller.tratarArgumentoInvalido(new IllegalArgumentException("x"));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, naoEncontrado.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, locacaoNaoEncontrada.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, conflito.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, proibido.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalido.getStatusCode());
        assertEquals(false, invalido.getBody().get("sucesso"));
        assertEquals("x", invalido.getBody().get("erro"));
    }

    // ---------- Ponta a ponta: gerar, assinar e baixar ----------

    // CT-E2E-01 | cenario normal: gerar, as duas partes assinam e o PDF mostra as duas assinaturas
    @Test
    void testCenarioNormalGerarAssinarPelasDuasPartesEBaixarPdf() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);

        // Action
        ContratoResponse gerado = controller.gerar(pedidoDeGerar("loc1")).getBody();
        ContratoResponse aposLocador = controller.assinar(gerado.getId(), pedidoDeAssinar("locador1")).getBody();
        ContratoResponse aposLocatario = controller.assinar(gerado.getId(), pedidoDeAssinar("locatario1")).getBody();
        ResponseEntity<byte[]> pdf = controller.baixarPdf(gerado.getId());

        // Assert
        assertFalse(gerado.isVigente());
        assertTrue(aposLocador.isAssinadoPeloLocador());
        assertFalse(aposLocador.isVigente());
        assertTrue(aposLocatario.isVigente());
        assertEquals(HttpStatus.OK, pdf.getStatusCode());
        assertTrue(pdf.getBody().length > 500);
    }

    // CT-E2E-02 | a tela busca primeiro pela locacao e acha o contrato que foi gerado
    @Test
    void testBuscarPorLocacaoAchaOContratoGerado() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        ContratoResponse gerado = controller.gerar(pedidoDeGerar("loc1")).getBody();

        // Action
        ContratoResponse encontrado = controller.buscarPorLocacao("loc1").getBody();

        // Assert
        assertEquals(gerado.getId(), encontrado.getId());
        assertEquals(gerado.getNumero(), encontrado.getNumero());
    }

    // CT-E2E-03 | excecao: locacao pendente nao gera contrato e nada fica guardado
    @Test
    void testCenarioDeExcecaoLocacaoPendenteNaoGeraContrato() {
        // Arrange
        cadastrarLocacao(StatusLocacao.PENDENTE);

        // Action
        ContratoConflitoException erro = assertThrows(ContratoConflitoException.class,
                () -> controller.gerar(pedidoDeGerar("loc1")));

        // Assert
        assertTrue(erro.getMessage().contains("PENDENTE"));
        assertTrue(bancoDeContratos.isEmpty());
    }

    // CT-E2E-04 | excecao: segunda geracao para a mesma locacao e recusada
    @Test
    void testCenarioDeExcecaoNaoGeraDoisContratosParaAMesmaLocacao() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        controller.gerar(pedidoDeGerar("loc1"));

        // Action
        ContratoConflitoException erro = assertThrows(ContratoConflitoException.class,
                () -> controller.gerar(pedidoDeGerar("loc1")));

        // Assert
        assertEquals("Esta locacao ja tem contrato.", erro.getMessage());
        assertEquals(1, bancoDeContratos.size());
    }

    // CT-E2E-05 | excecao: quem nao e parte nao assina e o contrato continua sem assinaturas
    @Test
    void testCenarioDeExcecaoUsuarioDeForaNaoAssinaEContratoContinuaIntacto() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        ContratoResponse gerado = controller.gerar(pedidoDeGerar("loc1")).getBody();

        // Action
        assertThrows(AssinaturaNaoPermitidaException.class,
                () -> controller.assinar(gerado.getId(), pedidoDeAssinar("intruso")));
        ContratoResponse depois = controller.buscar(gerado.getId()).getBody();

        // Assert
        assertFalse(depois.isAssinadoPeloLocador());
        assertFalse(depois.isAssinadoPeloLocatario());
    }

    // CT-E2E-06 | os numeros dos contratos seguem a ordem de geracao
    @Test
    void testNumerosDosContratosSaoSequenciais() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        Locacao segunda = new Locacao("locador1", "locatario1", "maq1",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3),
                new BigDecimal("850.00"), new BigDecimal("0.00"), new BigDecimal("2550.00"));
        segunda.setId("loc2");
        segunda.setStatus(StatusLocacao.ACEITO);
        when(locacaoRepository.findById("loc2")).thenReturn(Optional.of(segunda));

        // Action
        String primeiro = controller.gerar(pedidoDeGerar("loc1")).getBody().getNumero();
        String segundo = controller.gerar(pedidoDeGerar("loc2")).getBody().getNumero();

        // Assert
        int ano = LocalDate.now().getYear();
        assertEquals(Contrato.montarNumero(ano, 1), primeiro);
        assertEquals(Contrato.montarNumero(ano, 2), segundo);
    }
}
