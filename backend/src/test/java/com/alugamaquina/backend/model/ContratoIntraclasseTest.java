/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes OO Intraclasse (estados, atributos e categorias de metodos) da entidade Contrato
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Testes Intraclasse da classe Contrato (Issue #44), no padrao AAA (Arrange, Action, Assert).
 *
 * 1) Particionamento por ESTADOS
 *    Estados: EMITIDO (ninguem assinou) | ASSINADO_LOCADOR | ASSINADO_LOCATARIO | VIGENTE (as duas partes)
 *    Transicoes: EMITIDO -> ASSINADO_LOCADOR | EMITIDO -> ASSINADO_LOCATARIO |
 *                ASSINADO_LOCADOR -> VIGENTE | ASSINADO_LOCATARIO -> VIGENTE | assinar de novo (erro, fica no mesmo estado)
 *    Comportamento padrao: construtor - [assineComoLocador | assineComoLocatario]n - fim
 *
 * 2) Particionamento por ATRIBUTOS: valorTotal, valorCaucao, numero e dataEmissao
 *
 * 3) Particionamento por CATEGORIAS DE METODOS: inicializacao, computacao, setup (setters),
 *    consultas (getters), predicativas (retornam boolean) e canonicos (equals, hashCode, compareTo, toString)
 */
class ContratoIntraclasseTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 10);
    private static final LocalDate FIM = LocalDate.of(2026, 11, 14);
    private static final LocalDateTime EMISSAO = LocalDateTime.of(2026, 11, 1, 10, 0);

    // Contrato novo no estado EMITIDO, com emissao fixa para o equals
    private Contrato criarEmitido() throws Exception {
        Contrato contrato = new Contrato("loc1", "AM-2026-00123", "locador1", "locatario1", "maq1",
                "Carlos Eduardo", "Antonio Ferreira", "Trator John Deere", INICIO, FIM,
                new BigDecimal("4614.00"), new BigDecimal("1000.00"));
        contrato.setDataEmissao(EMISSAO);
        return contrato;
    }

    // =============== 1) ESTADOS ===============

    // CT-EST-01 | estado inicial: construtor deixa o contrato em EMITIDO
    @Test
    void testConstrutorDeixaOContratoEmitidoSemAssinaturas() throws Exception {
        // Arrange + Action
        Contrato contrato = criarEmitido();

        // Assert
        assertFalse(contrato.isAssinadoPeloLocador());
        assertFalse(contrato.isAssinadoPeloLocatario());
        assertFalse(contrato.isVigente());
        assertNull(contrato.getDataAssinaturaLocador());
        assertNull(contrato.getDataAssinaturaLocatario());
    }

    // CT-EST-02 | sequencia: construtor - assineComoLocador - assineComoLocatario
    @Test
    void testSequenciaLocadorDepoisLocatarioTerminaVigente() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action + Assert no ponto de cada transicao
        contrato.assineComoLocador();
        assertTrue(contrato.isAssinadoPeloLocador());          // EMITIDO -> ASSINADO_LOCADOR
        assertFalse(contrato.isVigente());

        contrato.assineComoLocatario();
        assertTrue(contrato.isAssinadoPeloLocatario());        // ASSINADO_LOCADOR -> VIGENTE
        assertTrue(contrato.isVigente());
    }

    // CT-EST-03 | sequencia: construtor - assineComoLocatario - assineComoLocador
    @Test
    void testSequenciaLocatarioDepoisLocadorTerminaVigente() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action + Assert no ponto de cada transicao
        contrato.assineComoLocatario();
        assertFalse(contrato.isVigente());                     // EMITIDO -> ASSINADO_LOCATARIO
        assertNull(contrato.getDataAssinaturaLocador());

        contrato.assineComoLocador();
        assertTrue(contrato.isVigente());                      // ASSINADO_LOCATARIO -> VIGENTE
    }

    // CT-EST-04 | sequencia: construtor - assineComoLocador - assineComoLocador (erro)
    @Test
    void testLocadorAssinarDuasVezesDaErroEMantemOEstado() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();
        contrato.assineComoLocador();
        LocalDateTime primeiraAssinatura = contrato.getDataAssinaturaLocador();

        // Action
        Exception erro = assertThrows(Exception.class, contrato::assineComoLocador);

        // Assert
        assertEquals("Locador ja assinou", erro.getMessage());
        assertEquals(primeiraAssinatura, contrato.getDataAssinaturaLocador());   // data nao muda
        assertFalse(contrato.isVigente());
    }

    // CT-EST-05 | sequencia: construtor - assineComoLocatario - assineComoLocatario (erro)
    @Test
    void testLocatarioAssinarDuasVezesDaErroEMantemOEstado() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();
        contrato.assineComoLocatario();

        // Action
        Exception erro = assertThrows(Exception.class, contrato::assineComoLocatario);

        // Assert
        assertEquals("Locatario ja assinou", erro.getMessage());
        assertFalse(contrato.isVigente());
    }

    // CT-EST-06 | sequencia: depois de VIGENTE qualquer nova assinatura da erro e continua VIGENTE
    @Test
    void testContratoVigenteNaoAceitaNovasAssinaturas() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();
        contrato.assineComoLocador();
        contrato.assineComoLocatario();

        // Action
        Exception erroLocador = assertThrows(Exception.class, contrato::assineComoLocador);
        Exception erroLocatario = assertThrows(Exception.class, contrato::assineComoLocatario);

        // Assert
        assertEquals("Locador ja assinou", erroLocador.getMessage());
        assertEquals("Locatario ja assinou", erroLocatario.getMessage());
        assertTrue(contrato.isVigente());
    }

    // CT-EST-07 | o toString muda conforme o estado
    @Test
    void testToStringMostraOEstadoDoContrato() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action
        String antes = contrato.toString();
        contrato.assineComoLocador();
        contrato.assineComoLocatario();
        String depois = contrato.toString();

        // Assert
        assertTrue(antes.contains("aguardando assinaturas"));
        assertTrue(depois.contains("vigente"));
        assertTrue(depois.contains("AM-2026-00123"));
    }

    // =============== 2) ATRIBUTOS ===============

    // CT-ATR-01 | atributo valorTotal: o setter normaliza e o getter devolve
    @Test
    void testAtributoValorTotalFicaComDuasCasasDecimais() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action
        contrato.setValorTotal(new BigDecimal("5000"));

        // Assert
        assertEquals(new BigDecimal("5000.00"), contrato.getValorTotal());
    }

    // CT-ATR-02 | atributo valorTotal: valor invalido nao altera o que ja existia
    @Test
    void testAtributoValorTotalInvalidoNaoAlteraOValorAnterior() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action
        Exception erro = assertThrows(Exception.class, () -> contrato.setValorTotal(BigDecimal.ZERO));

        // Assert
        assertEquals("Valor total invalido", erro.getMessage());
        assertEquals(new BigDecimal("4614.00"), contrato.getValorTotal());
    }

    // CT-ATR-03 | atributo valorCaucao: zero e aceito, negativo nao
    @Test
    void testAtributoValorCaucaoAceitaZeroERejeitaNegativo() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action
        contrato.setValorCaucao(BigDecimal.ZERO);
        Exception erro = assertThrows(Exception.class, () -> contrato.setValorCaucao(new BigDecimal("-5")));

        // Assert
        assertEquals(new BigDecimal("0.00"), contrato.getValorCaucao());
        assertEquals("Valor da caucao invalido", erro.getMessage());
    }

    // CT-ATR-04 | atributo numero: setter valida o formato
    @Test
    void testAtributoNumeroSoAceitaOFormatoCorreto() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action
        contrato.setNumero("AM-2026-00200");
        Exception erro = assertThrows(Exception.class, () -> contrato.setNumero("errado"));

        // Assert
        assertEquals("AM-2026-00200", contrato.getNumero());
        assertEquals("Numero do contrato invalido", erro.getMessage());
    }

    // CT-ATR-05 | atributo dataEmissao: define a ordem natural (compareTo)
    @Test
    void testAtributoDataEmissaoDefineAOrdemNatural() throws Exception {
        // Arrange
        Contrato antigo = criarEmitido();
        Contrato novo = criarEmitido();
        novo.setDataEmissao(EMISSAO.plusDays(1));

        // Action
        int antigoComNovo = antigo.compareTo(novo);
        int novoComAntigo = novo.compareTo(antigo);
        int igual = antigo.compareTo(criarEmitido());

        // Assert
        assertEquals(-666, antigoComNovo);
        assertEquals(666, novoComAntigo);
        assertEquals(0, igual);
    }

    // CT-ATR-06 | atributo dataEmissao nulo no compareTo
    @Test
    void testCompareToTrataDataDeEmissaoNula() throws Exception {
        // Arrange
        Contrato semData = criarEmitido();
        semData.setDataEmissao(null);
        Contrato comData = criarEmitido();
        Contrato outroSemData = criarEmitido();
        outroSemData.setDataEmissao(null);

        // Action + Assert
        assertEquals(-666, semData.compareTo(comData));
        assertEquals(666, comData.compareTo(semData));
        assertEquals(0, semData.compareTo(outroSemData));
    }

    // =============== 3) CATEGORIAS DE METODOS ===============

    // ---- Inicializacao: construtor vazio, de copia e clone ----

    // CT-MET-01 | construtor vazio (usado pelo Spring Data)
    @Test
    void testConstrutorVazioCriaObjetoSemDados() {
        // Arrange + Action
        Contrato contrato = new Contrato();

        // Assert
        assertNull(contrato.getId());
        assertNull(contrato.getNumero());
        assertFalse(contrato.isVigente());
    }

    // CT-MET-02 | construtor de copia copia todos os campos
    @Test
    void testConstrutorDeCopiaCopiaTodosOsCampos() throws Exception {
        // Arrange
        Contrato original = criarEmitido();
        original.assineComoLocador();

        // Action
        Contrato copia = new Contrato(original);

        // Assert
        assertEquals(original, copia);
        assertNotSame(original, copia);
        assertTrue(copia.isAssinadoPeloLocador());
    }

    // CT-MET-03 | construtor de copia com modelo nulo
    @Test
    void testConstrutorDeCopiaRejeitaModeloNulo() {
        // Arrange
        Contrato modelo = null;

        // Action
        Exception erro = assertThrows(Exception.class, () -> new Contrato(modelo));

        // Assert
        assertEquals("Modelo ausente", erro.getMessage());
    }

    // CT-MET-04 | clone gera copia independente do original
    @Test
    void testCloneGeraCopiaIndependenteDoOriginal() throws Exception {
        // Arrange
        Contrato original = criarEmitido();

        // Action
        Contrato clone = (Contrato) original.clone();
        clone.assineComoLocador();

        // Assert
        assertNotSame(original, clone);
        assertFalse(original.isAssinadoPeloLocador());
        assertTrue(clone.isAssinadoPeloLocador());
    }

    // ---- Computacao: montarNumero ----

    // CT-MET-05 | montarNumero completa com zeros
    @Test
    void testMontarNumeroCompletaComZeros() {
        // Arrange
        int ano = 2026;

        // Action
        String numero = Contrato.montarNumero(ano, 123);

        // Assert
        assertEquals("AM-2026-00123", numero);
    }

    // ---- Setup de valores: setters de texto ----

    // CT-MET-06 | setLocacaoId valida e apara
    @Test
    void testSetLocacaoIdValidaEApara() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();

        // Action
        contrato.setLocacaoId("  loc9  ");
        Exception erro = assertThrows(Exception.class, () -> contrato.setLocacaoId("  "));

        // Assert
        assertEquals("loc9", contrato.getLocacaoId());
        assertEquals("Locacao ausente", erro.getMessage());
    }

    // ---- Consultas: getters ----

    // CT-MET-07 | getters devolvem o que o construtor recebeu
    @Test
    void testGettersDevolvemOsDadosDoConstrutor() throws Exception {
        // Arrange + Action
        Contrato contrato = criarEmitido();

        // Assert
        assertEquals("locador1", contrato.getLocadorId());
        assertEquals("locatario1", contrato.getLocatarioId());
        assertEquals("maq1", contrato.getMaquinaId());
        assertEquals("Carlos Eduardo", contrato.getNomeLocador());
        assertEquals("Antonio Ferreira", contrato.getNomeLocatario());
        assertEquals("Trator John Deere", contrato.getDescricaoMaquina());
        assertEquals(INICIO, contrato.getDataInicio());
        assertEquals(FIM, contrato.getDataFim());
        assertNotNull(contrato.getDataEmissao());
    }

    // ---- Predicativas: metodos que retornam boolean ----

    // CT-MET-08 | isTextoValido com os dois resultados
    @Test
    void testIsTextoValidoRetornaVerdadeiroEFalso() {
        // Arrange + Action + Assert
        assertTrue(Contrato.isTextoValido("abc"));
        assertFalse(Contrato.isTextoValido(null));
        assertFalse(Contrato.isTextoValido("   "));
    }

    // CT-MET-09 | isNumeroValido com nulo
    @Test
    void testIsNumeroValidoComNulo() {
        // Arrange + Action + Assert
        assertFalse(Contrato.isNumeroValido(null));
        assertTrue(Contrato.isNumeroValido("AM-2026-00123"));
    }

    // ---- Canonicos: equals e hashCode ----

    // CT-CAN-01 | equals verdadeiro e hashCode igual para os mesmos dados
    @Test
    void testEqualsVerdadeiroEHashCodeIgualParaMesmosDados() throws Exception {
        // Arrange
        Contrato a = criarEmitido();
        Contrato b = criarEmitido();

        // Action
        boolean iguais = a.equals(b);

        // Assert
        assertTrue(iguais);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // CT-CAN-02 | equals ignora casas decimais, porque os valores sao normalizados
    @Test
    void testEqualsIgnoraDiferencaDeCasasDecimais() throws Exception {
        // Arrange
        Contrato a = criarEmitido();
        Contrato b = criarEmitido();

        // Action
        b.setValorTotal(new BigDecimal("4614"));

        // Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // CT-CAN-03 | equals falso para os 4 passos: nulo, outra classe, outro campo, mesma referencia
    @Test
    void testEqualsFalsoParaNuloOutraClasseEOutroEstado() throws Exception {
        // Arrange
        Contrato contrato = criarEmitido();
        Contrato assinado = criarEmitido();
        assinado.assineComoLocador();

        // Action + Assert
        assertTrue(contrato.equals(contrato));       // passo 1: mesmo endereco
        assertFalse(contrato.equals(null));          // passo 2: nulo
        assertFalse(contrato.equals("loc1"));        // passo 3: outra classe
        assertFalse(contrato.equals(assinado));      // passo 4: campo diferente
    }

    // CT-CAN-04 | hashCode diferente para contratos diferentes e sempre positivo
    @Test
    void testHashCodeDiferenteParaContratosDiferentesESempreNaoNegativo() throws Exception {
        // Arrange
        Contrato a = criarEmitido();
        Contrato b = criarEmitido();
        b.setNumero("AM-2026-00999");

        // Action
        int hashA = a.hashCode();
        int hashB = b.hashCode();

        // Assert
        assertTrue(hashA >= 0);
        assertTrue(hashB >= 0);
        assertTrue(hashA != hashB);
    }
}
