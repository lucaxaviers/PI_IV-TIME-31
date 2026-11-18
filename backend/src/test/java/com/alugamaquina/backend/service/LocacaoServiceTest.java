/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Testes JUnit 5 de LocacaoService no padrão AAA, com Particionamento de Equivalência
 *            e Análise de Valores de Fronteira (BVA).
 *
 * Partições testadas:
 *   - Período:      fim antes do início (inválido) | 1 diária (fronteira mínima) | várias diárias
 *   - Frete (km):   negativo (inválido) | 0 (retirada, sem frete) | 0,1 (primeiro valor com taxa) | maior
 *   - Transição:    PENDENTE para ACEITO ou RECUSADO (válida) | qualquer outro status (inválida)
 */

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.alugamaquina.backend.dto.LocacaoResponse;
import com.alugamaquina.backend.dto.SolicitacaoLocacaoRequest;
import com.alugamaquina.backend.exception.ConflitoAgendaException;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.TransicaoStatusInvalidaException;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.repository.LocacaoRepository;

class LocacaoServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 10);

    private LocacaoRepository locacaoRepository;
    private LocacaoService service;

    @BeforeEach
    // Cria o service com repositorio falso (mock) e um save que devolve o proprio objeto com id
    void setUp() {
        locacaoRepository = mock(LocacaoRepository.class);
        service = new LocacaoService(locacaoRepository);

        when(locacaoRepository.save(any(Locacao.class))).thenAnswer(invocation -> {
            Locacao locacao = invocation.getArgument(0);
            if (locacao.getId() == null) {
                locacao.setId("loc-1");
            }
            return locacao;
        });
    }

    // ---------- Apoio dos testes ----------

    private SolicitacaoLocacaoRequest pedido(LocalDate inicio, LocalDate fim, double distanciaKm) {
        return new SolicitacaoLocacaoRequest("locador-1", "locatario-1", "maq-1",
                inicio, fim, new BigDecimal("850.00"), distanciaKm);
    }

    private Locacao locacaoComStatus(StatusLocacao status) {
        Locacao locacao = new Locacao("locador-1", "locatario-1", "maq-1",
                INICIO, INICIO.plusDays(4),
                new BigDecimal("850.00"), new BigDecimal("475.00"), new BigDecimal("4725.00"));
        locacao.setId("loc-1");
        locacao.setStatus(status);
        return locacao;
    }

    // Define se o banco "acha" ou "nao acha" reserva aceita no periodo
    private void simularReservaAceita(boolean existe) {
        when(locacaoRepository.existsByMaquinaIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(existe);
    }

    // ---------- Solicitar: caminho feliz ----------

    @Test
    // Solicitacao valida: salva como PENDENTE, 5 diarias, frete de 25 km e total somado
    void deveSolicitarLocacaoPendenteComDiariasFreteETotal() {
        // Arrange: 10/11 a 14/11 = 5 diarias de 850 = 4250 ; frete 25 km = 25 x 2 x 6,50 + 150 = 475
        simularReservaAceita(false);
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO.plusDays(4), 25);

        // Act
        LocacaoResponse response = service.solicitar(request);

        // Assert
        assertTrue(response.isSucesso());
        assertEquals("loc-1", response.getId());
        assertEquals(StatusLocacao.PENDENTE, response.getStatus());
        assertEquals(5, response.getDiarias());
        assertEquals(new BigDecimal("850.00"), response.getValorDiaria());
        assertEquals(new BigDecimal("475.00"), response.getValorFrete());
        assertEquals(new BigDecimal("4725.00"), response.getValorTotal());

        ArgumentCaptor<Locacao> captor = ArgumentCaptor.forClass(Locacao.class);
        verify(locacaoRepository).save(captor.capture());
        Locacao salva = captor.getValue();
        assertEquals(StatusLocacao.PENDENTE, salva.getStatus());
        assertEquals("locador-1", salva.getLocadorId());
        assertEquals("locatario-1", salva.getLocatarioId());
        assertEquals("maq-1", salva.getMaquinaId());
    }

    @Test
    // Ids com espacos nas pontas sao salvos sem os espacos
    void deveRemoverEspacosDosIdsAoSalvar() {
        // Arrange
        simularReservaAceita(false);
        SolicitacaoLocacaoRequest request = new SolicitacaoLocacaoRequest("  locador-1 ", " locatario-1", "maq-1  ",
                INICIO, INICIO, new BigDecimal("850.00"), 0);

        // Act
        service.solicitar(request);

        // Assert
        ArgumentCaptor<Locacao> captor = ArgumentCaptor.forClass(Locacao.class);
        verify(locacaoRepository).save(captor.capture());
        assertEquals("locador-1", captor.getValue().getLocadorId());
        assertEquals("locatario-1", captor.getValue().getLocatarioId());
        assertEquals("maq-1", captor.getValue().getMaquinaId());
    }

    @Test
    // Valor da diaria sem casas decimais volta com 2 casas
    void deveNormalizarValorDaDiariaParaDuasCasas() {
        // Arrange
        simularReservaAceita(false);
        SolicitacaoLocacaoRequest request = new SolicitacaoLocacaoRequest("locador-1", "locatario-1", "maq-1",
                INICIO, INICIO, new BigDecimal("850"), 0);

        // Act
        LocacaoResponse response = service.solicitar(request);

        // Assert
        assertEquals(new BigDecimal("850.00"), response.getValorDiaria());
        assertEquals(new BigDecimal("850.00"), response.getValorTotal());
    }

    // ---------- BVA: periodo ----------

    @Test
    // Fronteira minima: inicio e fim no mesmo dia = 1 diaria (850 + frete de 25 km = 1325)
    void locacaoDeUmDiaDeveContarUmaDiaria() {
        // Arrange
        simularReservaAceita(false);
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 25);

        // Act
        LocacaoResponse response = service.solicitar(request);

        // Assert
        assertEquals(1, response.getDiarias());
        assertEquals(new BigDecimal("1325.00"), response.getValorTotal());
    }

    @Test
    // Fronteira invalida: fim um dia antes do inicio
    void dataFimAnteriorAoInicioDeveLancarExcecao() {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO.minusDays(1), 25);

        // Act + Assert
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.solicitar(request));
        assertTrue(erro.getMessage().contains("termino"));
        verify(locacaoRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "2026-11-10, 2026-11-10, 1",
            "2026-11-10, 2026-11-11, 2",
            "2026-11-10, 2026-11-14, 5",
            "2026-12-31, 2027-01-01, 2"
    })
    // Diarias contam o primeiro e o ultimo dia, tambem na virada de mes e de ano
    void deveCalcularDiariasContandoPrimeiroEUltimoDia(LocalDate inicio, LocalDate fim, long esperado) {
        // Arrange (dados vindos da tabela acima)

        // Act
        long diarias = LocacaoService.calcularDiarias(inicio, fim);

        // Assert
        assertEquals(esperado, diarias);
    }

    // ---------- BVA: frete ----------

    @ParameterizedTest
    @CsvSource({
            "0, 0.00",       // retirada no local: sem frete e sem taxa
            "0.1, 151.30",   // primeiro valor acima de zero: ja paga a taxa de 150,00
            "1, 163.00",
            "25, 475.00",
            "100, 1450.00"
    })
    // Frete = km x 2 x 6,50 + 150,00 (ou zero quando km = 0)
    void deveCalcularFretePelaDistancia(double km, String esperado) {
        // Arrange (dados vindos da tabela acima)

        // Act
        BigDecimal frete = LocacaoService.calcularFrete(km);

        // Assert
        assertEquals(new BigDecimal(esperado), frete);
    }

    @Test
    // Frete com 0 km (retirada no local): total e so o valor das diarias
    void freteComZeroKmDeveSerZeroENaoSomarNoTotal() {
        // Arrange: 5 diarias de 850 = 4250
        simularReservaAceita(false);
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO.plusDays(4), 0);

        // Act
        LocacaoResponse response = service.solicitar(request);

        // Assert
        assertEquals(new BigDecimal("0.00"), response.getValorFrete());
        assertEquals(new BigDecimal("4250.00"), response.getValorTotal());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, -1, -500, Double.NaN, Double.POSITIVE_INFINITY})
    // Distancia negativa ou invalida nao existe
    void distanciaInvalidaDeveLancarExcecao(double kmInvalido) {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, kmInvalido);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
        verify(locacaoRepository, never()).save(any());
    }

    // ---------- Conflito de agenda ----------

    @Test
    // Maquina com reserva ACEITA no periodo: bloqueia a nova solicitacao
    void deveBloquearSolicitacaoQuandoMaquinaTemReservaAceitaNoPeriodo() {
        // Arrange
        simularReservaAceita(true);
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO.plusDays(4), 25);

        // Act + Assert
        assertThrows(ConflitoAgendaException.class, () -> service.solicitar(request));
        verify(locacaoRepository, never()).save(any());
    }

    @Test
    // A consulta de conflito recebe a maquina, o status ACEITO, o fim e o inicio (nessa ordem)
    void deveConsultarConflitoSomenteDeReservasAceitasDaMesmaMaquina() {
        // Arrange
        simularReservaAceita(false);
        LocalDate fim = INICIO.plusDays(4);

        // Act
        service.solicitar(pedido(INICIO, fim, 25));

        // Assert
        verify(locacaoRepository)
                .existsByMaquinaIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                        eq("maq-1"), eq(StatusLocacao.ACEITO), eq(fim), eq(INICIO));
    }

    // ---------- Validacao dos dados ----------

    @Test
    // Pedido nulo
    void solicitacaoNulaDeveLancarExcecao() {
        // Arrange (nada a preparar)

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    // Locador nulo, vazio ou em branco
    void locadorSemIdDeveLancarExcecao(String locadorInvalido) {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 0);
        request.setLocadorId(locadorInvalido);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    // Locatario nulo, vazio ou em branco
    void locatarioSemIdDeveLancarExcecao(String locatarioInvalido) {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 0);
        request.setLocatarioId(locatarioInvalido);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    // Maquina nula, vazia ou em branco
    void maquinaSemIdDeveLancarExcecao(String maquinaInvalida) {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 0);
        request.setMaquinaId(maquinaInvalida);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @Test
    // Sem data de inicio
    void dataInicioNulaDeveLancarExcecao() {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(null, INICIO, 0);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @Test
    // Sem data de termino
    void dataFimNulaDeveLancarExcecao() {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, null, 0);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @Test
    // Sem valor da diaria
    void valorDaDiariaNuloDeveLancarExcecao() {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 0);
        request.setValorDiaria(null);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-850.00"})
    // Fronteira invalida: zero e negativos
    void valorDaDiariaZeroOuNegativoDeveLancarExcecao(String valorInvalido) {
        // Arrange
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 0);
        request.setValorDiaria(new BigDecimal(valorInvalido));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.solicitar(request));
    }

    @Test
    // Fronteira valida: 1 centavo
    void valorDaDiariaDeUmCentavoDeveSerAceito() {
        // Arrange
        simularReservaAceita(false);
        SolicitacaoLocacaoRequest request = pedido(INICIO, INICIO, 0);
        request.setValorDiaria(new BigDecimal("0.01"));

        // Act
        LocacaoResponse response = assertDoesNotThrow(() -> service.solicitar(request));

        // Assert
        assertEquals(new BigDecimal("0.01"), response.getValorTotal());
    }

    // ---------- Aceitar: transicoes de status ----------

    @Test
    // Transicao valida: PENDENTE -> ACEITO
    void deveAceitarLocacaoPendente() {
        // Arrange
        Locacao pendente = locacaoComStatus(StatusLocacao.PENDENTE);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(pendente));
        simularReservaAceita(false);

        // Act
        LocacaoResponse response = service.aceitar("loc-1");

        // Assert
        assertTrue(response.isSucesso());
        assertEquals(StatusLocacao.ACEITO, response.getStatus());
        assertEquals(StatusLocacao.ACEITO, pendente.getStatus());
        verify(locacaoRepository).save(pendente);
    }

    @Test
    // Transicao invalida: CANCELADO -> ACEITO
    void aceitarLocacaoCanceladaDeveLancarTransicaoInvalida() {
        // Arrange
        Locacao cancelada = locacaoComStatus(StatusLocacao.CANCELADO);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(cancelada));

        // Act + Assert
        TransicaoStatusInvalidaException erro = assertThrows(TransicaoStatusInvalidaException.class,
                () -> service.aceitar("loc-1"));
        assertTrue(erro.getMessage().contains("CANCELADO"));
        assertEquals(StatusLocacao.CANCELADO, cancelada.getStatus());
        verify(locacaoRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusLocacao.class, names = "PENDENTE", mode = EnumSource.Mode.EXCLUDE)
    // Particao invalida: qualquer status diferente de PENDENTE nao pode ser aceito
    void aceitarLocacaoForaDePendenteDeveLancarTransicaoInvalida(StatusLocacao statusAtual) {
        // Arrange
        Locacao locacao = locacaoComStatus(statusAtual);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao));

        // Act + Assert
        assertThrows(TransicaoStatusInvalidaException.class, () -> service.aceitar("loc-1"));
        assertEquals(statusAtual, locacao.getStatus());
        verify(locacaoRepository, never()).save(any());
    }

    @Test
    // Outra reserva foi aceita no periodo depois do pedido: nao deixa aceitar
    void aceitarComConflitoDeAgendaDeveLancarExcecaoEManterPendente() {
        // Arrange
        Locacao pendente = locacaoComStatus(StatusLocacao.PENDENTE);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(pendente));
        simularReservaAceita(true);

        // Act + Assert
        assertThrows(ConflitoAgendaException.class, () -> service.aceitar("loc-1"));
        assertEquals(StatusLocacao.PENDENTE, pendente.getStatus());
        verify(locacaoRepository, never()).save(any());
    }

    @Test
    // Id que nao existe no banco
    void aceitarLocacaoInexistenteDeveLancarExcecao() {
        // Arrange
        when(locacaoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(LocacaoNaoEncontradaException.class, () -> service.aceitar("nao-existe"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    // Id nulo, vazio ou em branco
    void aceitarSemIdDeveLancarExcecao(String idInvalido) {
        // Arrange (nada a preparar)

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.aceitar(idInvalido));
    }

    // ---------- Recusar: transicoes de status ----------

    @Test
    // Transicao valida: PENDENTE -> RECUSADO, com o motivo sem espacos nas pontas
    void deveRecusarLocacaoPendenteGuardandoOMotivo() {
        // Arrange
        Locacao pendente = locacaoComStatus(StatusLocacao.PENDENTE);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(pendente));

        // Act
        LocacaoResponse response = service.recusar("loc-1", "  Maquina em manutencao  ");

        // Assert
        assertEquals(StatusLocacao.RECUSADO, response.getStatus());
        assertEquals("Maquina em manutencao", response.getMotivoRecusa());
        assertEquals(StatusLocacao.RECUSADO, pendente.getStatus());
        verify(locacaoRepository).save(pendente);
    }

    @Test
    // O motivo e opcional
    void deveRecusarLocacaoSemMotivo() {
        // Arrange
        Locacao pendente = locacaoComStatus(StatusLocacao.PENDENTE);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(pendente));

        // Act
        LocacaoResponse response = service.recusar("loc-1", null);

        // Assert
        assertEquals(StatusLocacao.RECUSADO, response.getStatus());
        assertEquals("", response.getMotivoRecusa());
    }

    @ParameterizedTest
    @EnumSource(value = StatusLocacao.class, names = "PENDENTE", mode = EnumSource.Mode.EXCLUDE)
    // Particao invalida: so PENDENTE pode ser recusada
    void recusarLocacaoForaDePendenteDeveLancarTransicaoInvalida(StatusLocacao statusAtual) {
        // Arrange
        Locacao locacao = locacaoComStatus(statusAtual);
        when(locacaoRepository.findById("loc-1")).thenReturn(Optional.of(locacao));

        // Act + Assert
        assertThrows(TransicaoStatusInvalidaException.class, () -> service.recusar("loc-1", "motivo"));
        assertEquals(statusAtual, locacao.getStatus());
        verify(locacaoRepository, never()).save(any());
    }

    @Test
    // Id que nao existe no banco
    void recusarLocacaoInexistenteDeveLancarExcecao() {
        // Arrange
        when(locacaoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(LocacaoNaoEncontradaException.class, () -> service.recusar("nao-existe", null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    // Id nulo, vazio ou em branco
    void recusarSemIdDeveLancarExcecao(String idInvalido) {
        // Arrange (nada a preparar)

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.recusar(idInvalido, "motivo"));
    }
}
