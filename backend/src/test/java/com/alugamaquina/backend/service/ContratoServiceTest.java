/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes unitarios do ContratoService com repositorios simulados (Particionamento de Equivalencia)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.alugamaquina.backend.dto.ContratoResponse;
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
 * Requisito de teste RT-03 (Issue #44): gerar contrato so de locacao aceita, e assinar so quem e parte.
 *
 * Particionamento de Equivalencia
 *   gerar(locacaoId)   V1 locacao ACEITA sem contrato
 *                      I1 id nulo/vazio | I2 locacao inexistente | I3 status diferente de ACEITO
 *                      I4 contrato ja existe | I5 locador, locatario ou maquina inexistente
 *   assinar(id, user)  V1 locador | V2 locatario | V3 mesma pessoa nas duas partes
 *                      I1 usuario vazio | I2 contrato inexistente | I3 usuario de fora | I4 assinar de novo
 *
 * Os repositorios sao simulados (mock), porque o objeto do teste e so a regra do servico.
 */
class ContratoServiceTest {

    private ContratoRepository contratoRepository;
    private LocacaoRepository locacaoRepository;
    private UsuarioRepository usuarioRepository;
    private MaquinaRepository maquinaRepository;
    private GeradorPdfContrato geradorPdfContrato;
    private ContratoService servico;

    @BeforeEach
    void preparar() {
        contratoRepository = mock(ContratoRepository.class);
        locacaoRepository = mock(LocacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        maquinaRepository = mock(MaquinaRepository.class);
        geradorPdfContrato = mock(GeradorPdfContrato.class);
        servico = new ContratoService(contratoRepository, locacaoRepository, usuarioRepository,
                maquinaRepository, geradorPdfContrato);

        // O save devolve o proprio objeto recebido
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(chamada -> chamada.getArgument(0));
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

    private Usuario criarUsuario(String id, String nome) {
        Usuario usuario = new Usuario(nome, "12345678901", "19999990000", id + "@teste.com",
                "Paulinia", "ambos", "hash");
        usuario.setId(id);
        return usuario;
    }

    // Deixa tudo pronto para gerar: locacao aceita, partes e maquina existentes
    private void prepararLocacaoAceita() {
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.of(criarLocacao(StatusLocacao.ACEITO)));
        when(usuarioRepository.findById("locador1")).thenReturn(Optional.of(criarUsuario("locador1", "Carlos Eduardo")));
        when(usuarioRepository.findById("locatario1")).thenReturn(Optional.of(criarUsuario("locatario1", "Antonio Ferreira")));
        Maquina maquina = new Maquina();
        maquina.setId("maq1");
        maquina.setTituloAnuncio("Trator John Deere 6115J");
        when(maquinaRepository.findById("maq1")).thenReturn(Optional.of(maquina));
        when(contratoRepository.existsByLocacaoId("loc1")).thenReturn(false);
        when(contratoRepository.count()).thenReturn(0L);
    }

    private Contrato criarContratoSalvo() throws Exception {
        Contrato contrato = new Contrato("loc1", "AM-2026-00001", "locador1", "locatario1", "maq1",
                "Carlos Eduardo", "Antonio Ferreira", "Trator", LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 14), new BigDecimal("4614.00"), new BigDecimal("1000.00"));
        contrato.setId("con1");
        return contrato;
    }

    // ---------- gerar ----------

    // CT-SRV-01 | V1: locacao aceita gera contrato com os dados reais
    @Test
    void testGerarContratoDeLocacaoAceitaGuardaOsDadosDaLocacao() {
        // Arrange
        prepararLocacaoAceita();

        // Action
        ContratoResponse resposta = servico.gerar("loc1");

        // Assert
        assertTrue(resposta.isSucesso());
        assertEquals("Carlos Eduardo", resposta.getNomeLocador());
        assertEquals("Antonio Ferreira", resposta.getNomeLocatario());
        assertEquals("Trator John Deere 6115J", resposta.getDescricaoMaquina());
        assertEquals(new BigDecimal("4614.00"), resposta.getValorTotal());
        assertEquals(ContratoService.VALOR_CAUCAO_PADRAO, resposta.getValorCaucao());
        assertEquals(5, resposta.getDiarias());
        assertFalse(resposta.isVigente());
    }

    // CT-SRV-02 | V1: o contrato e salvo uma vez
    @Test
    void testGerarSalvaOContratoUmaVez() {
        // Arrange
        prepararLocacaoAceita();

        // Action
        servico.gerar("loc1");

        // Assert
        ArgumentCaptor<Contrato> capturado = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(capturado.capture());
        assertEquals("loc1", capturado.getValue().getLocacaoId());
    }

    // CT-SRV-03 | V1: numero sequencial a partir da contagem (BVA: 0 contratos -> 00001, 41 -> 00042)
    @Test
    void testGerarNumeraOContratoPelaContagemDeContratos() {
        // Arrange
        prepararLocacaoAceita();
        int ano = LocalDate.now().getYear();

        // Action
        String primeiro = servico.gerar("loc1").getNumero();
        when(contratoRepository.count()).thenReturn(41L);
        String quadragesimoSegundo = servico.gerar("loc1").getNumero();

        // Assert
        assertEquals(Contrato.montarNumero(ano, 1), primeiro);
        assertEquals(Contrato.montarNumero(ano, 42), quadragesimoSegundo);
    }

    // CT-SRV-04 | I1: id nulo ou vazio
    @ParameterizedTest(name = "CT-SRV-04 locacaoId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testGerarRejeitaIdDeLocacaoVazio(String locacaoId) {
        // Arrange - nenhum dado, so o id invalido

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.gerar(locacaoId));

        // Assert
        assertEquals("Identificador da locacao e obrigatorio.", erro.getMessage());
        verify(contratoRepository, never()).save(any());
    }

    // CT-SRV-05 | I2: locacao inexistente
    @Test
    void testGerarComLocacaoInexistenteDaErro() {
        // Arrange
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.empty());

        // Action
        LocacaoNaoEncontradaException erro = assertThrows(LocacaoNaoEncontradaException.class,
                () -> servico.gerar("loc1"));

        // Assert
        assertEquals("Locacao nao encontrada: loc1", erro.getMessage());
    }

    // CT-SRV-06 | I3: todos os status diferentes de ACEITO
    @ParameterizedTest(name = "CT-SRV-06 status={0}")
    @EnumSource(value = StatusLocacao.class, names = "ACEITO", mode = EnumSource.Mode.EXCLUDE)
    void testGerarSoPermiteLocacaoAceita(StatusLocacao status) {
        // Arrange
        when(locacaoRepository.findById("loc1")).thenReturn(Optional.of(criarLocacao(status)));

        // Action
        ContratoConflitoException erro = assertThrows(ContratoConflitoException.class, () -> servico.gerar("loc1"));

        // Assert
        assertTrue(erro.getMessage().contains(status.name()));
        verify(contratoRepository, never()).save(any());
    }

    // CT-SRV-07 | I4: locacao que ja tem contrato
    @Test
    void testGerarNaoCriaSegundoContratoParaAMesmaLocacao() {
        // Arrange
        prepararLocacaoAceita();
        when(contratoRepository.existsByLocacaoId("loc1")).thenReturn(true);

        // Action
        ContratoConflitoException erro = assertThrows(ContratoConflitoException.class, () -> servico.gerar("loc1"));

        // Assert
        assertEquals("Esta locacao ja tem contrato.", erro.getMessage());
        verify(contratoRepository, never()).save(any());
    }

    // CT-SRV-08 | I5: locador inexistente
    @Test
    void testGerarComLocadorInexistenteDaErro() {
        // Arrange
        prepararLocacaoAceita();
        when(usuarioRepository.findById("locador1")).thenReturn(Optional.empty());

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.gerar("loc1"));

        // Assert
        assertEquals("Locador nao encontrado: locador1", erro.getMessage());
    }

    // CT-SRV-09 | I5: locatario inexistente
    @Test
    void testGerarComLocatarioInexistenteDaErro() {
        // Arrange
        prepararLocacaoAceita();
        when(usuarioRepository.findById("locatario1")).thenReturn(Optional.empty());

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.gerar("loc1"));

        // Assert
        assertEquals("Locatario nao encontrado: locatario1", erro.getMessage());
    }

    // CT-SRV-10 | I5: maquina inexistente
    @Test
    void testGerarComMaquinaInexistenteDaErro() {
        // Arrange
        prepararLocacaoAceita();
        when(maquinaRepository.findById("maq1")).thenReturn(Optional.empty());

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.gerar("loc1"));

        // Assert
        assertEquals("Maquina nao encontrada: maq1", erro.getMessage());
    }

    // ---------- buscar ----------

    // CT-SRV-11 | V1: contrato existente
    @Test
    void testBuscarDevolveOContratoPeloId() throws Exception {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(criarContratoSalvo()));

        // Action
        ContratoResponse resposta = servico.buscar("con1");

        // Assert
        assertEquals("AM-2026-00001", resposta.getNumero());
        assertEquals("con1", resposta.getId());
    }

    // CT-SRV-12 | I2: contrato inexistente
    @Test
    void testBuscarContratoInexistenteDaErro() {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.empty());

        // Action
        ContratoNaoEncontradoException erro = assertThrows(ContratoNaoEncontradoException.class,
                () -> servico.buscar("con1"));

        // Assert
        assertEquals("Contrato nao encontrado: con1", erro.getMessage());
    }

    // CT-SRV-13 | buscar pela locacao: existe e nao existe
    @Test
    void testBuscarPorLocacaoEncontraOuAvisa() throws Exception {
        // Arrange
        when(contratoRepository.findByLocacaoId("loc1")).thenReturn(Optional.of(criarContratoSalvo()));
        when(contratoRepository.findByLocacaoId("loc2")).thenReturn(Optional.empty());

        // Action
        ContratoResponse encontrado = servico.buscarPorLocacao("loc1");
        ContratoNaoEncontradoException erro = assertThrows(ContratoNaoEncontradoException.class,
                () -> servico.buscarPorLocacao("loc2"));

        // Assert
        assertEquals("loc1", encontrado.getLocacaoId());
        assertEquals("Nao ha contrato para a locacao: loc2", erro.getMessage());
    }

    // ---------- assinar ----------

    // CT-SRV-14 | V1: o locador assina
    @Test
    void testLocadorAssinaEContratoAguardaOLocatario() throws Exception {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(criarContratoSalvo()));

        // Action
        ContratoResponse resposta = servico.assinar("con1", "locador1");

        // Assert
        assertTrue(resposta.isAssinadoPeloLocador());
        assertFalse(resposta.isAssinadoPeloLocatario());
        assertFalse(resposta.isVigente());
        assertEquals("Assinatura registrada. Aguardando a outra parte.", resposta.getMensagem());
    }

    // CT-SRV-15 | V2: o locatario assina
    @Test
    void testLocatarioAssinaEContratoAguardaOLocador() throws Exception {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(criarContratoSalvo()));

        // Action
        ContratoResponse resposta = servico.assinar("con1", "locatario1");

        // Assert
        assertTrue(resposta.isAssinadoPeloLocatario());
        assertFalse(resposta.isAssinadoPeloLocador());
        assertFalse(resposta.isVigente());
    }

    // CT-SRV-16 | V1 + V2: as duas partes assinam e o contrato fica vigente
    @Test
    void testContratoFicaVigenteQuandoAsDuasPartesAssinam() throws Exception {
        // Arrange
        Contrato contrato = criarContratoSalvo();
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(contrato));

        // Action
        servico.assinar("con1", "locador1");
        ContratoResponse resposta = servico.assinar("con1", "locatario1");

        // Assert
        assertTrue(resposta.isVigente());
        assertEquals("Contrato assinado pelas duas partes.", resposta.getMensagem());
    }

    // CT-SRV-17 | V3: mesma pessoa e locador e locatario, assina duas vezes (uma por papel)
    @Test
    void testMesmaPessoaNasDuasPartesAssinaPrimeiroComoLocadorDepoisComoLocatario() throws Exception {
        // Arrange
        Contrato contrato = new Contrato("loc1", "AM-2026-00001", "pessoa1", "pessoa1", "maq1",
                "Maria", "Maria", "Trator", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14),
                new BigDecimal("4614.00"), new BigDecimal("1000.00"));
        contrato.setId("con1");
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(contrato));

        // Action
        ContratoResponse primeira = servico.assinar("con1", "pessoa1");
        ContratoResponse segunda = servico.assinar("con1", "pessoa1");

        // Assert
        assertTrue(primeira.isAssinadoPeloLocador());
        assertFalse(primeira.isVigente());
        assertTrue(segunda.isVigente());
    }

    // CT-SRV-18 | I1: usuario vazio
    @ParameterizedTest(name = "CT-SRV-18 usuarioId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testAssinarRejeitaUsuarioVazio(String usuarioId) {
        // Arrange - usuario invalido

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.assinar("con1", usuarioId));

        // Assert
        assertEquals("Identificador do usuario e obrigatorio.", erro.getMessage());
    }

    // CT-SRV-19 | I2: contrato inexistente
    @Test
    void testAssinarContratoInexistenteDaErro() {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.empty());

        // Action
        ContratoNaoEncontradoException erro = assertThrows(ContratoNaoEncontradoException.class,
                () -> servico.assinar("con1", "locador1"));

        // Assert
        assertEquals("Contrato nao encontrado: con1", erro.getMessage());
    }

    // CT-SRV-20 | I3: usuario que nao e parte do contrato
    @Test
    void testUsuarioDeForaNaoPodeAssinar() throws Exception {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(criarContratoSalvo()));

        // Action
        AssinaturaNaoPermitidaException erro = assertThrows(AssinaturaNaoPermitidaException.class,
                () -> servico.assinar("con1", "intruso"));

        // Assert
        assertEquals("Somente o locador ou o locatario podem assinar este contrato.", erro.getMessage());
        verify(contratoRepository, never()).save(any());
    }

    // CT-SRV-21 | I4: o locador assina de novo
    @Test
    void testLocadorNaoAssinaDuasVezes() throws Exception {
        // Arrange
        Contrato contrato = criarContratoSalvo();
        contrato.assineComoLocador();
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(contrato));

        // Action
        ContratoConflitoException erro = assertThrows(ContratoConflitoException.class,
                () -> servico.assinar("con1", "locador1"));

        // Assert
        assertEquals("Locador ja assinou", erro.getMessage());
        verify(contratoRepository, never()).save(any());
    }

    // CT-SRV-22 | I4: o locatario assina de novo
    @Test
    void testLocatarioNaoAssinaDuasVezes() throws Exception {
        // Arrange
        Contrato contrato = criarContratoSalvo();
        contrato.assineComoLocatario();
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(contrato));

        // Action
        ContratoConflitoException erro = assertThrows(ContratoConflitoException.class,
                () -> servico.assinar("con1", "locatario1"));

        // Assert
        assertEquals("Locatario ja assinou", erro.getMessage());
    }

    // ---------- PDF ----------

    // CT-SRV-23 | gerarPdf pede o PDF ao gerador com o contrato certo
    @Test
    void testGerarPdfUsaOGeradorComOContratoDoId() throws Exception {
        // Arrange
        Contrato contrato = criarContratoSalvo();
        byte[] esperado = "%PDF-falso".getBytes();
        when(contratoRepository.findById("con1")).thenReturn(Optional.of(contrato));
        when(geradorPdfContrato.gerar(contrato)).thenReturn(esperado);

        // Action
        byte[] pdf = servico.gerarPdf("con1");

        // Assert
        assertEquals(esperado, pdf);
        verify(geradorPdfContrato).gerar(contrato);
    }

    // CT-SRV-24 | gerarPdf de contrato inexistente
    @Test
    void testGerarPdfDeContratoInexistenteDaErro() {
        // Arrange
        when(contratoRepository.findById("con1")).thenReturn(Optional.empty());

        // Action + Assert
        assertThrows(ContratoNaoEncontradoException.class, () -> servico.gerarPdf("con1"));
        verify(geradorPdfContrato, never()).gerar(any());
    }
}
