/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes OO Interclasse (integracao e ponta a ponta) do fluxo da vistoria tecnica
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.alugamaquina.backend.controller.VistoriaChecklistController;
import com.alugamaquina.backend.dto.ConcluirVistoriaRequest;
import com.alugamaquina.backend.dto.CriarVistoriaRequest;
import com.alugamaquina.backend.dto.ItemChecklistDto;
import com.alugamaquina.backend.dto.VistoriaResponse;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.VistoriaConflitoException;
import com.alugamaquina.backend.exception.VistoriaNaoEncontradaException;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.model.StatusVistoria;
import com.alugamaquina.backend.model.TipoVistoria;
import com.alugamaquina.backend.model.VistoriaChecklist;
import com.alugamaquina.backend.repository.LocacaoRepository;
import com.alugamaquina.backend.repository.VistoriaChecklistRepository;

/**
 * Testes de integracao entre classes (Issue #43), no padrao AAA.
 *
 * Estrategia incremental (duas classes por vez):
 *   VistoriaChecklistController -> VistoriaChecklistService  (resposta e erros viram HTTP)
 *   VistoriaChecklistService -> VistoriaChecklist -> ItemChecklist (o servico usa as regras do modelo)
 *
 * Estrategia ponta a ponta (thread "abrir a retirada, preencher e concluir; depois a devolucao"):
 *   Controller -> Service -> repositorios (simulados em memoria) -> VistoriaChecklist -> ItemChecklist
 */
class VistoriaInterclasseTest {

    private Map<String, VistoriaChecklist> bancoDeVistorias;
    private VistoriaChecklistRepository vistoriaRepository;
    private LocacaoRepository locacaoRepository;
    private VistoriaChecklistController controller;

    @BeforeEach
    void preparar() {
        bancoDeVistorias = new HashMap<>();
        vistoriaRepository = mock(VistoriaChecklistRepository.class);
        locacaoRepository = mock(LocacaoRepository.class);

        // Repositorio de vistorias em memoria
        when(vistoriaRepository.save(any(VistoriaChecklist.class))).thenAnswer(chamada -> {
            VistoriaChecklist vistoria = chamada.getArgument(0);
            if (vistoria.getId() == null) {
                vistoria.setId("vis" + (bancoDeVistorias.size() + 1));
            }
            bancoDeVistorias.put(vistoria.getId(), vistoria);
            return vistoria;
        });
        when(vistoriaRepository.findById(anyString()))
                .thenAnswer(chamada -> Optional.ofNullable(bancoDeVistorias.get(chamada.<String>getArgument(0))));
        when(vistoriaRepository.findByLocacaoId(anyString())).thenAnswer(chamada -> bancoDeVistorias.values().stream()
                .filter(vistoria -> vistoria.getLocacaoId().equals(chamada.getArgument(0))).toList());
        when(vistoriaRepository.existsByLocacaoIdAndTipo(anyString(), any(TipoVistoria.class)))
                .thenAnswer(chamada -> bancoDeVistorias.values().stream()
                        .anyMatch(vistoria -> vistoria.getLocacaoId().equals(chamada.getArgument(0))
                                && vistoria.getTipo() == chamada.getArgument(1)));

        controller = new VistoriaChecklistController(new VistoriaChecklistService(vistoriaRepository, locacaoRepository));
    }

    // ---------- Massa de dados ----------

    private void cadastrarLocacao(StatusLocacao status) {
        Locacao locacao = new Locacao("locador1", "locatario1", "maq1",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14),
                new BigDecimal("850.00"), new BigDecimal("364.00"), new BigDecimal("4614.00"));
        locacao.setId("loc1");
        locacao.setStatus(status);
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.of(locacao));
    }

    private CriarVistoriaRequest pedidoDeAbrir(TipoVistoria tipo, String responsavelId) {
        CriarVistoriaRequest pedido = new CriarVistoriaRequest();
        pedido.setLocacaoId("loc1");
        pedido.setTipo(tipo);
        pedido.setResponsavelId(responsavelId);
        return pedido;
    }

    // Responde todos os itens do checklist como conformes
    private ConcluirVistoriaRequest pedidoDeConcluirTudoConforme(VistoriaResponse aberta, double horimetro) {
        List<ItemChecklistDto> respostas = new ArrayList<>();
        for (ItemChecklistDto item : aberta.getItens()) {
            respostas.add(new ItemChecklistDto(item.getDescricao(), true, null));
        }
        ConcluirVistoriaRequest pedido = new ConcluirVistoriaRequest();
        pedido.setHorimetro(horimetro);
        pedido.setItens(respostas);
        return pedido;
    }

    // ---------- Integracao incremental: Controller -> Servico ----------

    // CT-VINT-01 | abrir responde 201 com a vistoria
    @Test
    void testControllerAbrirResponde201ComAVistoriaPendente() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);

        // Action
        ResponseEntity<VistoriaResponse> resposta = controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1"));

        // Assert
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody().getId());
        assertEquals(StatusVistoria.PENDENTE, resposta.getBody().getStatus());
    }

    // CT-VINT-02 | pedido nulo vira erro de argumento
    @Test
    void testControllerAbrirSemCorpoDaErroDeArgumento() {
        // Arrange - pedido nulo

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> controller.abrir(null));

        // Assert
        assertEquals("Dados da vistoria sao obrigatorios.", erro.getMessage());
    }

    // CT-VINT-03 | os tratadores de erro devolvem o status HTTP certo
    @Test
    void testControllerTraduzCadaExcecaoParaOStatusHttpCerto() {
        // Arrange - uma excecao de cada tipo

        // Action
        ResponseEntity<Map<String, Object>> naoEncontrada =
                controller.tratarNaoEncontrado(new VistoriaNaoEncontradaException("x"));
        ResponseEntity<Map<String, Object>> locacaoNaoEncontrada =
                controller.tratarNaoEncontrado(new LocacaoNaoEncontradaException("x"));
        ResponseEntity<Map<String, Object>> conflito = controller.tratarConflito(new VistoriaConflitoException("x"));
        ResponseEntity<Map<String, Object>> invalido = controller.tratarArgumentoInvalido(new IllegalArgumentException("x"));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, naoEncontrada.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, locacaoNaoEncontrada.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, conflito.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalido.getStatusCode());
        assertEquals(false, invalido.getBody().get("sucesso"));
        assertEquals("x", invalido.getBody().get("erro"));
    }

    // ---------- Integracao incremental: Servico -> Modelo ----------

    // CT-VINT-04 | o servico usa a regra do modelo: nao conforme sem observacao e recusado
    @Test
    void testServicoAplicaARegraDeObservacaoDoModelo() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        VistoriaResponse aberta = controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1")).getBody();
        ConcluirVistoriaRequest pedido = pedidoDeConcluirTudoConforme(aberta, 100.0);
        pedido.getItens().get(0).setConforme(false);
        pedido.getItens().get(0).setObservacao("");

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> controller.concluir(aberta.getId(), pedido));

        // Assert
        assertEquals("Item nao conforme exige observacao", erro.getMessage());
        assertEquals(StatusVistoria.PENDENTE, controller.buscar(aberta.getId()).getBody().getStatus());
    }

    // ---------- Ponta a ponta ----------

    // CT-VE2E-01 | cenario normal: abrir, concluir sem pendencias e consultar pela locacao
    @Test
    void testCenarioNormalAbrirConcluirEConsultarPelaLocacao() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);

        // Action
        VistoriaResponse aberta = controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1")).getBody();
        VistoriaResponse concluida = controller.concluir(aberta.getId(), pedidoDeConcluirTudoConforme(aberta, 1200.5)).getBody();
        List<VistoriaResponse> daLocacao = controller.listarPorLocacao("loc1").getBody();

        // Assert
        assertEquals(StatusVistoria.CONCLUIDA, concluida.getStatus());
        assertFalse(concluida.isTemPendencias());
        assertEquals(1200.5, concluida.getHorimetro());
        assertEquals(1, daLocacao.size());
        assertEquals(StatusVistoria.CONCLUIDA, daLocacao.get(0).getStatus());
    }

    // CT-VE2E-02 | cenario com pendencia: um item nao conforme deixa a vistoria COM_PENDENCIAS
    @Test
    void testCenarioComItemNaoConformeTerminaComPendencias() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        VistoriaResponse aberta = controller.abrir(pedidoDeAbrir(TipoVistoria.DEVOLUCAO, "locatario1")).getBody();
        ConcluirVistoriaRequest pedido = pedidoDeConcluirTudoConforme(aberta, 1350.0);
        pedido.getItens().get(7).setConforme(false);
        pedido.getItens().get(7).setObservacao("amassado na lateral esquerda");

        // Action
        VistoriaResponse concluida = controller.concluir(aberta.getId(), pedido).getBody();

        // Assert
        assertEquals(StatusVistoria.COM_PENDENCIAS, concluida.getStatus());
        assertTrue(concluida.isTemPendencias());
        assertEquals("amassado na lateral esquerda", concluida.getItens().get(7).getObservacao());
    }

    // CT-VE2E-03 | retirada e devolucao da mesma locacao convivem
    @Test
    void testRetiradaEDevolucaoDaMesmaLocacaoConvivem() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);

        // Action
        controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1"));
        controller.abrir(pedidoDeAbrir(TipoVistoria.DEVOLUCAO, "locador1"));
        List<VistoriaResponse> daLocacao = controller.listarPorLocacao("loc1").getBody();

        // Assert
        assertEquals(2, daLocacao.size());
    }

    // CT-VE2E-04 | excecao: segunda vistoria do mesmo tipo e recusada
    @Test
    void testCenarioDeExcecaoNaoAbreDuasVistoriasDoMesmoTipo() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1"));

        // Action
        VistoriaConflitoException erro = assertThrows(VistoriaConflitoException.class,
                () -> controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locatario1")));

        // Assert
        assertEquals("Esta locacao ja tem vistoria de RETIRADA.", erro.getMessage());
        assertEquals(1, bancoDeVistorias.size());
    }

    // CT-VE2E-05 | excecao: locacao pendente nao tem vistoria e nada fica guardado
    @Test
    void testCenarioDeExcecaoLocacaoPendenteNaoAbreVistoria() {
        // Arrange
        cadastrarLocacao(StatusLocacao.PENDENTE);

        // Action
        VistoriaConflitoException erro = assertThrows(VistoriaConflitoException.class,
                () -> controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1")));

        // Assert
        assertTrue(erro.getMessage().contains("PENDENTE"));
        assertTrue(bancoDeVistorias.isEmpty());
    }

    // CT-VE2E-06 | excecao: vistoria concluida nao aceita nova conclusao e mantem o resultado
    @Test
    void testCenarioDeExcecaoVistoriaConcluidaNaoPodeSerConcluidaDeNovo() {
        // Arrange
        cadastrarLocacao(StatusLocacao.ACEITO);
        VistoriaResponse aberta = controller.abrir(pedidoDeAbrir(TipoVistoria.RETIRADA, "locador1")).getBody();
        controller.concluir(aberta.getId(), pedidoDeConcluirTudoConforme(aberta, 500.0));

        // Action
        VistoriaConflitoException erro = assertThrows(VistoriaConflitoException.class,
                () -> controller.concluir(aberta.getId(), pedidoDeConcluirTudoConforme(aberta, 999.0)));

        // Assert
        assertEquals("Esta vistoria ja foi concluida.", erro.getMessage());
        assertEquals(500.0, controller.buscar(aberta.getId()).getBody().getHorimetro());
    }
}
