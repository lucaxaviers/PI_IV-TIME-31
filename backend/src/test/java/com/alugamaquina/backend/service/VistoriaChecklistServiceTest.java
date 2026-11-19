/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes unitarios do VistoriaChecklistService com repositorios simulados (Equivalencia e BVA)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.alugamaquina.backend.dto.ConcluirVistoriaRequest;
import com.alugamaquina.backend.dto.CriarVistoriaRequest;
import com.alugamaquina.backend.dto.ItemChecklistDto;
import com.alugamaquina.backend.dto.VistoriaResponse;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.VistoriaConflitoException;
import com.alugamaquina.backend.exception.VistoriaNaoEncontradaException;
import com.alugamaquina.backend.model.ItemChecklist;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.model.StatusVistoria;
import com.alugamaquina.backend.model.TipoVistoria;
import com.alugamaquina.backend.model.VistoriaChecklist;
import com.alugamaquina.backend.repository.LocacaoRepository;
import com.alugamaquina.backend.repository.VistoriaChecklistRepository;

/**
 * Requisito de teste RT-06 (Issue #43): abrir vistoria so de locacao aceita e concluir so com checklist completo.
 *
 * Particionamento de Equivalencia
 *   abrir(request)    V1 locacao ACEITA, responsavel e uma das partes, tipo ainda nao aberto
 *                     I1 pedido/ids/tipo ausentes | I2 locacao inexistente | I3 status diferente de ACEITO
 *                     I4 responsavel de fora | I5 tipo ja aberto
 *   concluir(id, req) V1 todos conformes | V2 algum nao conforme (COM_PENDENCIAS)
 *                     I1 vistoria inexistente | I2 ja concluida | I3 sem itens | I4 item sem resposta
 *                     I5 nao conforme sem observacao | I6 horimetro invalido
 *
 * BVA do horimetro no concluir: -0,1 / 0 / 0,1.
 */
class VistoriaChecklistServiceTest {

    private Map<String, VistoriaChecklist> bancoDeVistorias;
    private VistoriaChecklistRepository vistoriaRepository;
    private LocacaoRepository locacaoRepository;
    private VistoriaChecklistService servico;

    @BeforeEach
    void preparar() {
        vistoriaRepository = mock(VistoriaChecklistRepository.class);
        locacaoRepository = mock(LocacaoRepository.class);
        servico = new VistoriaChecklistService(vistoriaRepository, locacaoRepository);

        // Banco em memoria: o save guarda a vistoria e o findById devolve o que foi guardado
        bancoDeVistorias = new HashMap<>();
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
    }

    // ---------- Massa de dados ----------

    private Locacao criarLocacao(StatusLocacao status) {
        Locacao locacao = new Locacao("locador1", "locatario1", "maq1",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14),
                new BigDecimal("850.00"), new BigDecimal("364.00"), new BigDecimal("4614.00"));
        locacao.setId("loc1");
        locacao.setStatus(status);
        return locacao;
    }

    private CriarVistoriaRequest pedidoDeAbrir(String locacaoId, TipoVistoria tipo, String responsavelId) {
        CriarVistoriaRequest pedido = new CriarVistoriaRequest();
        pedido.setLocacaoId(locacaoId);
        pedido.setTipo(tipo);
        pedido.setResponsavelId(responsavelId);
        return pedido;
    }

    // Vistoria PENDENTE guardada no banco simulado
    private VistoriaChecklist guardarVistoriaPendente() throws Exception {
        List<ItemChecklist> itens = new ArrayList<>();
        for (String descricao : VistoriaChecklistService.ITENS_PADRAO) {
            itens.add(new ItemChecklist(descricao));
        }
        VistoriaChecklist vistoria = new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "locador1", itens);
        vistoria.setId("vis1");
        bancoDeVistorias.put("vis1", vistoria);
        return vistoria;
    }

    // Resposta de todos os itens padrao, todos conformes
    private List<ItemChecklistDto> respostasConformes() {
        List<ItemChecklistDto> respostas = new ArrayList<>();
        for (String descricao : VistoriaChecklistService.ITENS_PADRAO) {
            respostas.add(new ItemChecklistDto(descricao, true, null));
        }
        return respostas;
    }

    private ConcluirVistoriaRequest pedidoDeConcluir(Double horimetro, List<ItemChecklistDto> itens) {
        ConcluirVistoriaRequest pedido = new ConcluirVistoriaRequest();
        pedido.setHorimetro(horimetro);
        pedido.setItens(itens);
        return pedido;
    }

    private void prepararLocacaoAceita() {
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.of(criarLocacao(StatusLocacao.ACEITO)));
        when(vistoriaRepository.existsByLocacaoIdAndTipo(any(), any())).thenReturn(false);
    }

    // ---------- abrir ----------

    // CT-VSRV-01 | V1: abre com os 8 itens padrao sem resposta
    @Test
    void testAbrirCriaVistoriaPendenteComOsItensPadraoSemResposta() {
        // Arrange
        prepararLocacaoAceita();

        // Action
        VistoriaResponse resposta = servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.RETIRADA, "locador1"));

        // Assert
        assertTrue(resposta.isSucesso());
        assertEquals(StatusVistoria.PENDENTE, resposta.getStatus());
        assertEquals(VistoriaChecklistService.ITENS_PADRAO.size(), resposta.getItens().size());
        assertEquals(8, resposta.getItens().size());
        assertTrue(resposta.getItens().stream().allMatch(item -> item.getConforme() == null));
        assertEquals("maq1", resposta.getMaquinaId());
    }

    // CT-VSRV-02 | V1: o locatario tambem pode abrir a vistoria de devolucao
    @Test
    void testAbrirPeloLocatarioNaDevolucaoFunciona() {
        // Arrange
        prepararLocacaoAceita();

        // Action
        VistoriaResponse resposta = servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.DEVOLUCAO, "locatario1"));

        // Assert
        assertEquals(TipoVistoria.DEVOLUCAO, resposta.getTipo());
        assertEquals("locatario1", resposta.getResponsavelId());
    }

    // CT-VSRV-03 | I1: pedido nulo
    @Test
    void testAbrirComPedidoNuloDaErro() {
        // Arrange - pedido nulo

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.abrir(null));

        // Assert
        assertEquals("Dados da vistoria sao obrigatorios.", erro.getMessage());
    }

    // CT-VSRV-04 | I1: id da locacao vazio
    @ParameterizedTest(name = "CT-VSRV-04 locacaoId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testAbrirRejeitaLocacaoVazia(String locacaoId) {
        // Arrange - locacao invalida

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.abrir(pedidoDeAbrir(locacaoId, TipoVistoria.RETIRADA, "locador1")));

        // Assert
        assertEquals("Identificador da locacao e obrigatorio.", erro.getMessage());
    }

    // CT-VSRV-05 | I1: responsavel vazio
    @ParameterizedTest(name = "CT-VSRV-05 responsavelId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testAbrirRejeitaResponsavelVazio(String responsavelId) {
        // Arrange - responsavel invalido

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.RETIRADA, responsavelId)));

        // Assert
        assertEquals("Identificador do responsavel e obrigatorio.", erro.getMessage());
    }

    // CT-VSRV-06 | I1: tipo ausente
    @Test
    void testAbrirSemTipoDaErro() {
        // Arrange - tipo nulo

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.abrir(pedidoDeAbrir("loc1", null, "locador1")));

        // Assert
        assertEquals("Tipo da vistoria e obrigatorio.", erro.getMessage());
    }

    // CT-VSRV-07 | I2: locacao inexistente
    @Test
    void testAbrirComLocacaoInexistenteDaErro() {
        // Arrange
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.empty());

        // Action
        LocacaoNaoEncontradaException erro = assertThrows(LocacaoNaoEncontradaException.class,
                () -> servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.RETIRADA, "locador1")));

        // Assert
        assertEquals("Locacao nao encontrada: loc1", erro.getMessage());
    }

    // CT-VSRV-08 | I3: todos os status diferentes de ACEITO
    @ParameterizedTest(name = "CT-VSRV-08 status={0}")
    @EnumSource(value = StatusLocacao.class, names = "ACEITO", mode = EnumSource.Mode.EXCLUDE)
    void testAbrirSoPermiteLocacaoAceita(StatusLocacao status) {
        // Arrange
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.of(criarLocacao(status)));

        // Action
        VistoriaConflitoException erro = assertThrows(VistoriaConflitoException.class,
                () -> servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.RETIRADA, "locador1")));

        // Assert
        assertTrue(erro.getMessage().contains(status.name()));
        verify(vistoriaRepository, never()).save(any());
    }

    // CT-VSRV-09 | I4: responsavel que nao e parte da locacao
    @Test
    void testAbrirComResponsavelDeForaDaErro() {
        // Arrange
        prepararLocacaoAceita();

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.RETIRADA, "intruso")));

        // Assert
        assertEquals("O responsavel precisa ser o locador ou o locatario da locacao.", erro.getMessage());
        verify(vistoriaRepository, never()).save(any());
    }

    // CT-VSRV-10 | I5: o mesmo tipo nao abre duas vezes
    @Test
    void testAbrirNaoRepeteOMesmoTipoNaLocacao() {
        // Arrange
        prepararLocacaoAceita();
        when(vistoriaRepository.existsByLocacaoIdAndTipo("loc1", TipoVistoria.RETIRADA)).thenReturn(true);

        // Action
        VistoriaConflitoException erro = assertThrows(VistoriaConflitoException.class,
                () -> servico.abrir(pedidoDeAbrir("loc1", TipoVistoria.RETIRADA, "locador1")));

        // Assert
        assertEquals("Esta locacao ja tem vistoria de RETIRADA.", erro.getMessage());
        verify(vistoriaRepository, never()).save(any());
    }

    // ---------- concluir ----------

    // CT-VSRV-11 | V1: todos conformes conclui sem pendencias
    @Test
    void testConcluirComTudoConformeFicaConcluida() throws Exception {
        // Arrange
        guardarVistoriaPendente();

        // Action
        VistoriaResponse resposta = servico.concluir("vis1", pedidoDeConcluir(1200.5, respostasConformes()));

        // Assert
        assertEquals(StatusVistoria.CONCLUIDA, resposta.getStatus());
        assertFalse(resposta.isTemPendencias());
        assertEquals(1200.5, resposta.getHorimetro());
        assertEquals("Vistoria concluida sem pendencias.", resposta.getMensagem());
    }

    // CT-VSRV-12 | V2: item nao conforme conclui com pendencias
    @Test
    void testConcluirComItemNaoConformeFicaComPendencias() throws Exception {
        // Arrange
        guardarVistoriaPendente();
        List<ItemChecklistDto> respostas = respostasConformes();
        respostas.set(4, new ItemChecklistDto("Freios funcionando", false, "freio dianteiro fraco"));

        // Action
        VistoriaResponse resposta = servico.concluir("vis1", pedidoDeConcluir(300.0, respostas));

        // Assert
        assertEquals(StatusVistoria.COM_PENDENCIAS, resposta.getStatus());
        assertTrue(resposta.isTemPendencias());
        assertEquals("Vistoria concluida com pendencias.", resposta.getMensagem());
    }

    // CT-VSRV-13 | V1: salva a vistoria concluida uma vez
    @Test
    void testConcluirSalvaAVistoriaUmaVez() throws Exception {
        // Arrange
        guardarVistoriaPendente();

        // Action
        servico.concluir("vis1", pedidoDeConcluir(10.0, respostasConformes()));

        // Assert
        verify(vistoriaRepository).save(any(VistoriaChecklist.class));
    }

    // CT-VSRV-14 | I1: vistoria inexistente
    @Test
    void testConcluirVistoriaInexistenteDaErro() {
        // Arrange - banco sem nenhuma vistoria

        // Action
        VistoriaNaoEncontradaException erro = assertThrows(VistoriaNaoEncontradaException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(1.0, respostasConformes())));

        // Assert
        assertEquals("Vistoria nao encontrada: vis1", erro.getMessage());
    }

    // CT-VSRV-15 | I2: vistoria ja concluida
    @Test
    void testConcluirDuasVezesDaConflito() throws Exception {
        // Arrange
        guardarVistoriaPendente();
        servico.concluir("vis1", pedidoDeConcluir(1.0, respostasConformes()));

        // Action
        VistoriaConflitoException erro = assertThrows(VistoriaConflitoException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(2.0, respostasConformes())));

        // Assert
        assertEquals("Esta vistoria ja foi concluida.", erro.getMessage());
    }

    // CT-VSRV-16 | I3: pedido nulo ou sem itens
    @Test
    void testConcluirSemPedidoOuSemItensDaErro() throws Exception {
        // Arrange
        guardarVistoriaPendente();

        // Action
        IllegalArgumentException semPedido = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", null));
        IllegalArgumentException semItens = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(1.0, new ArrayList<>())));
        IllegalArgumentException itensNulos = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(1.0, null)));

        // Assert
        assertEquals("Dados da conclusao sao obrigatorios.", semPedido.getMessage());
        assertEquals("Responda os itens do checklist.", semItens.getMessage());
        assertEquals("Responda os itens do checklist.", itensNulos.getMessage());
    }

    // CT-VSRV-17 | I4: item sem marcar conforme ou nao conforme
    @Test
    void testConcluirComItemSemRespostaDaErro() throws Exception {
        // Arrange
        guardarVistoriaPendente();
        List<ItemChecklistDto> respostas = respostasConformes();
        respostas.set(0, new ItemChecklistDto("Pneus, rodas ou esteiras sem avarias", null, null));

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(1.0, respostas)));

        // Assert
        assertEquals("Todos os itens precisam ser marcados como conforme ou nao conforme", erro.getMessage());
    }

    // CT-VSRV-18 | I4: faltou responder um dos itens
    @Test
    void testConcluirSemResponderTodosOsItensDaErro() throws Exception {
        // Arrange
        guardarVistoriaPendente();
        List<ItemChecklistDto> respostas = respostasConformes();
        respostas.remove(0);

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(1.0, respostas)));

        // Assert
        assertEquals("Responda todos os itens do checklist", erro.getMessage());
    }

    // CT-VSRV-19 | I5: nao conforme sem observacao
    @Test
    void testConcluirComNaoConformeSemObservacaoDaErro() throws Exception {
        // Arrange
        guardarVistoriaPendente();
        List<ItemChecklistDto> respostas = respostasConformes();
        respostas.set(4, new ItemChecklistDto("Freios funcionando", false, "  "));

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(1.0, respostas)));

        // Assert
        assertEquals("Item nao conforme exige observacao", erro.getMessage());
    }

    // CT-VSRV-20 | I6 + BVA: horimetro no limite (-0,1 invalido, 0 e 0,1 validos)
    @ParameterizedTest(name = "CT-VSRV-20 horimetro={0} -> valido={1}")
    @CsvSource({"-0.1, false", "0.0, true", "0.1, true"})
    void testBvaDoHorimetroAoConcluir(double horimetro, boolean esperado) throws Exception {
        // Arrange
        guardarVistoriaPendente();

        // Action + Assert
        if (esperado) {
            assertEquals(StatusVistoria.CONCLUIDA,
                    servico.concluir("vis1", pedidoDeConcluir(horimetro, respostasConformes())).getStatus());
        } else {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> servico.concluir("vis1", pedidoDeConcluir(horimetro, respostasConformes())));
            assertEquals("Horimetro invalido", erro.getMessage());
        }
    }

    // CT-VSRV-21 | I6: horimetro nulo
    @Test
    void testConcluirSemHorimetroDaErro() throws Exception {
        // Arrange
        guardarVistoriaPendente();

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.concluir("vis1", pedidoDeConcluir(null, respostasConformes())));

        // Assert
        assertEquals("Horimetro invalido", erro.getMessage());
    }

    // CT-VSRV-22 | erro na conclusao nao deixa a vistoria pela metade: nada e salvo
    @Test
    void testConcluirComErroNaoSalvaENaoAlteraAVistoriaOriginal() throws Exception {
        // Arrange
        VistoriaChecklist original = guardarVistoriaPendente();
        List<ItemChecklistDto> respostas = respostasConformes();
        respostas.set(7, new ItemChecklistDto("Avarias visiveis (riscos, amassados, vazamentos)", false, ""));

        // Action
        assertThrows(IllegalArgumentException.class, () -> servico.concluir("vis1", pedidoDeConcluir(5.0, respostas)));

        // Assert
        verify(vistoriaRepository, never()).save(any());
        assertEquals(StatusVistoria.PENDENTE, original.getStatus());
        assertFalse(original.getItens().get(0).isRespondido());     // as respostas validas tambem nao ficaram
    }

    // ---------- buscar e listar ----------

    // CT-VSRV-23 | buscar existente e inexistente
    @Test
    void testBuscarDevolveAVistoriaOuDaErro() throws Exception {
        // Arrange
        guardarVistoriaPendente();

        // Action
        VistoriaResponse encontrada = servico.buscar("vis1");

        // Assert
        assertEquals("vis1", encontrada.getId());
        assertThrows(VistoriaNaoEncontradaException.class, () -> servico.buscar("vis2"));
        assertThrows(IllegalArgumentException.class, () -> servico.buscar(" "));
    }

    // CT-VSRV-24 | listar por locacao
    @Test
    void testListarPorLocacaoDevolveAsVistoriasDaLocacao() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = guardarVistoriaPendente();
        when(vistoriaRepository.findByLocacaoId("loc1")).thenReturn(List.of(vistoria));

        // Action
        List<VistoriaResponse> lista = servico.listarPorLocacao("loc1");

        // Assert
        assertEquals(1, lista.size());
        assertEquals(TipoVistoria.RETIRADA, lista.get(0).getTipo());
        assertThrows(IllegalArgumentException.class, () -> servico.listarPorLocacao(""));
    }
}
