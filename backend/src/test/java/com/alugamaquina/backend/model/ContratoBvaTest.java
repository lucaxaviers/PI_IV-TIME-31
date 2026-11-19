/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes de Caixa-Preta (Particionamento de Equivalencia e BVA) da entidade Contrato
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Requisito de teste RT-01 (Issue #44): o contrato so pode ser criado com dados validos.
 *
 * Particionamento de Equivalencia (classes validas e invalidas)
 *   Entrada                        | Valida                    | Invalida
 *   textos (ids, nomes, descricao) | V1 texto com conteudo     | I1 nulo, I2 vazio ou so espacos
 *   numero                         | V1 AM-AAAA-NNNNN          | I1 nulo, I2 fora do formato
 *   periodo                        | V1 fim igual/depois       | I1 fim antes do inicio, I2 data nula
 *   valorTotal                     | V1 maior que zero         | I1 zero, I2 negativo, I3 nulo
 *   valorCaucao                    | V1 zero ou maior          | I1 negativo, I2 nulo
 *
 * BVA: para cada limite testa-se o limite, o valor logo abaixo e o valor logo acima.
 *
 * Cada teste faz uma coisa so e segue o padrao AAA (Arrange, Action, Assert).
 */
class ContratoBvaTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 10);
    private static final LocalDate FIM = LocalDate.of(2026, 11, 14);

    // Monta um contrato valido; cada teste troca so o dado que esta sendo testado
    private Contrato criar(String locacaoId, String numero, String locadorId, String locatarioId, String maquinaId,
                           String nomeLocador, String nomeLocatario, String descricao,
                           LocalDate inicio, LocalDate fim, BigDecimal total, BigDecimal caucao) throws Exception {
        return new Contrato(locacaoId, numero, locadorId, locatarioId, maquinaId,
                nomeLocador, nomeLocatario, descricao, inicio, fim, total, caucao);
    }

    private Contrato criarValido() throws Exception {
        return criar("loc1", "AM-2026-00123", "locador1", "locatario1", "maq1",
                "Carlos Eduardo", "Antonio Ferreira", "Trator John Deere", INICIO, FIM,
                new BigDecimal("4614.00"), new BigDecimal("1000.00"));
    }

    // ---------- Classes de equivalencia validas ----------

    // CT-EQ-01 | V1 de todas as entradas
    @Test
    void testConstrutorComTodasAsClassesValidasCriaContrato() throws Exception {
        // Arrange - nenhum dado extra, usa a massa valida

        // Action
        Contrato contrato = criarValido();

        // Assert
        assertNotNull(contrato);
        assertEquals("AM-2026-00123", contrato.getNumero());
        assertEquals(new BigDecimal("4614.00"), contrato.getValorTotal());
    }

    // CT-EQ-02 | texto com espacos nas pontas e aparado
    @Test
    void testConstrutorApagaEspacosDasPontasDosTextos() throws Exception {
        // Arrange
        String locacaoComEspacos = "  loc1  ";

        // Action
        Contrato contrato = criar(locacaoComEspacos, "AM-2026-00123", "locador1", "locatario1", "maq1",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO);

        // Assert
        assertEquals("loc1", contrato.getLocacaoId());
    }

    // ---------- Classes de equivalencia invalidas: textos ----------

    // CT-EQ-03 | I1 e I2 do campo locacaoId
    @ParameterizedTest(name = "CT-EQ-03 locacaoId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaLocacaoInvalida(String locacaoId) {
        // Arrange - locacaoId invalido, demais dados validos

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar(locacaoId, "AM-2026-00123", "l", "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Locacao ausente", erro.getMessage());
    }

    // CT-EQ-04 | I1 e I2 do campo locadorId
    @ParameterizedTest(name = "CT-EQ-04 locadorId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaLocadorInvalido(String locadorId) {
        // Arrange - locadorId invalido

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", locadorId, "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Locador ausente", erro.getMessage());
    }

    // CT-EQ-05 | I1 e I2 do campo locatarioId
    @ParameterizedTest(name = "CT-EQ-05 locatarioId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaLocatarioInvalido(String locatarioId) {
        // Arrange - locatarioId invalido

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", locatarioId, "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Locatario ausente", erro.getMessage());
    }

    // CT-EQ-06 | I1 e I2 do campo maquinaId
    @ParameterizedTest(name = "CT-EQ-06 maquinaId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaMaquinaInvalida(String maquinaId) {
        // Arrange - maquinaId invalido

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", maquinaId,
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Maquina ausente", erro.getMessage());
    }

    // CT-EQ-07 | I1 e I2 dos nomes das partes e da descricao
    @ParameterizedTest(name = "CT-EQ-07 nomeLocador=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaNomeDoLocadorInvalido(String nome) {
        // Arrange - nome do locador invalido

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", "m",
                nome, "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Nome do locador ausente", erro.getMessage());
    }

    @ParameterizedTest(name = "CT-EQ-08 nomeLocatario=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaNomeDoLocatarioInvalido(String nome) {
        // Arrange - nome do locatario invalido

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", "m",
                "Carlos", nome, "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Nome do locatario ausente", erro.getMessage());
    }

    @ParameterizedTest(name = "CT-EQ-09 descricaoMaquina=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaDescricaoDaMaquinaInvalida(String descricao) {
        // Arrange - descricao invalida

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", "m",
                "Carlos", "Antonio", descricao, INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Descricao da maquina ausente", erro.getMessage());
    }

    // ---------- Classes de equivalencia invalidas: numero, periodo, valores ----------

    // CT-EQ-10 | I2 do numero: formatos errados
    @ParameterizedTest(name = "CT-EQ-10 numero=[{0}]")
    @ValueSource(strings = {"XX-2026-00123", "am-2026-00123", "AM2026-00123", "AM-2026-00A23", "2026-00123", ""})
    void testConstrutorRejeitaNumeroForaDoFormato(String numero) {
        // Arrange - numero fora do formato AM-AAAA-NNNNN

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", numero, "l", "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Numero do contrato invalido", erro.getMessage());
    }

    // CT-EQ-11 | I1 do numero: nulo
    @Test
    void testConstrutorRejeitaNumeroNulo() {
        // Arrange - numero nulo

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", null, "l", "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Numero do contrato invalido", erro.getMessage());
    }

    // CT-EQ-12 | I1 do periodo: fim antes do inicio
    @Test
    void testConstrutorRejeitaFimAntesDoInicio() {
        // Arrange - periodo invertido

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", "m",
                "Carlos", "Antonio", "Trator", FIM, INICIO, new BigDecimal("100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Periodo invalido", erro.getMessage());
    }

    // CT-EQ-13 | I2 do periodo: datas nulas
    @Test
    void testPeriodoComDataNulaEInvalido() {
        // Arrange - uma data nula de cada vez

        // Action + Assert
        assertFalse(Contrato.isPeriodoValido(null, FIM));
        assertFalse(Contrato.isPeriodoValido(INICIO, null));
        assertFalse(Contrato.isPeriodoValido(null, null));
    }

    // CT-EQ-14 | I3 do valorTotal e I2 da caucao: nulos
    @Test
    void testValoresNulosSaoInvalidos() {
        // Arrange - valores nulos

        // Action + Assert
        assertFalse(Contrato.isValorTotalValido(null));
        assertFalse(Contrato.isValorCaucaoValido(null));
    }

    // CT-EQ-15 | I2 do valorTotal: negativo
    @Test
    void testConstrutorRejeitaValorTotalNegativo() {
        // Arrange - total negativo

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("-100"), BigDecimal.ZERO));

        // Assert
        assertEquals("Valor total invalido", erro.getMessage());
    }

    // CT-EQ-16 | I1 da caucao: negativa
    @Test
    void testConstrutorRejeitaCaucaoNegativa() {
        // Arrange - caucao negativa

        // Action
        Exception erro = assertThrows(Exception.class, () -> criar("loc1", "AM-2026-00123", "l", "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, new BigDecimal("100"), new BigDecimal("-1")));

        // Assert
        assertEquals("Valor da caucao invalido", erro.getMessage());
    }

    // ---------- BVA: limite, abaixo do limite e acima do limite ----------

    // CT-BVA-01 | periodo: diferenca de dias entre inicio e fim (limite = 0 dias)
    @ParameterizedTest(name = "CT-BVA-01 fim = inicio + {0} dias -> valido={1}")
    @CsvSource({"-1, false", "0, true", "1, true"})
    void testBvaDoPeriodoEmDias(int dias, boolean esperado) {
        // Arrange
        LocalDate fim = INICIO.plusDays(dias);

        // Action
        boolean valido = Contrato.isPeriodoValido(INICIO, fim);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-02 | valorTotal (limite = 0,01)
    @ParameterizedTest(name = "CT-BVA-02 valorTotal={0} -> valido={1}")
    @CsvSource({"0.00, false", "0.01, true", "0.02, true"})
    void testBvaDoValorTotal(String valor, boolean esperado) {
        // Arrange
        BigDecimal total = new BigDecimal(valor);

        // Action
        boolean valido = Contrato.isValorTotalValido(total);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-03 | valorCaucao (limite = 0,00)
    @ParameterizedTest(name = "CT-BVA-03 valorCaucao={0} -> valido={1}")
    @CsvSource({"-0.01, false", "0.00, true", "0.01, true"})
    void testBvaDaCaucao(String valor, boolean esperado) {
        // Arrange
        BigDecimal caucao = new BigDecimal(valor);

        // Action
        boolean valido = Contrato.isValorCaucaoValido(caucao);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-04 | sequencia do numero (limite = exatamente 5 digitos)
    @ParameterizedTest(name = "CT-BVA-04 numero={0} -> valido={1}")
    @CsvSource({"AM-2026-0123, false", "AM-2026-00123, true", "AM-2026-001234, false",
            "AM-2026-00000, true", "AM-2026-99999, true"})
    void testBvaDaSequenciaDoNumero(String numero, boolean esperado) {
        // Arrange - numero com 4, 5 e 6 digitos de sequencia, e os extremos 00000 e 99999

        // Action
        boolean valido = Contrato.isNumeroValido(numero);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-05 | ano do numero (limite = exatamente 4 digitos)
    @ParameterizedTest(name = "CT-BVA-05 numero={0} -> valido={1}")
    @CsvSource({"AM-202-00123, false", "AM-2026-00123, true", "AM-20266-00123, false"})
    void testBvaDoAnoDoNumero(String numero, boolean esperado) {
        // Arrange - ano com 3, 4 e 5 digitos

        // Action
        boolean valido = Contrato.isNumeroValido(numero);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-06 | montarNumero na fronteira da sequencia (99999 cabe, 100000 nao cabe)
    @Test
    void testBvaDoMontarNumeroNoMaiorValorQueCabe() {
        // Arrange
        long maior = 99999;

        // Action
        String cabe = Contrato.montarNumero(2026, maior);
        String estoura = Contrato.montarNumero(2026, maior + 1);

        // Assert
        assertTrue(Contrato.isNumeroValido(cabe));
        assertFalse(Contrato.isNumeroValido(estoura));
        assertEquals("AM-2026-00001", Contrato.montarNumero(2026, 1));
    }

    // CT-BVA-07 | casas decimais: 850 e 850.00 sao o mesmo valor
    @Test
    void testValoresSaoNormalizadosParaDuasCasasDecimais() throws Exception {
        // Arrange
        BigDecimal semCasas = new BigDecimal("850");
        BigDecimal umaCasa = new BigDecimal("0.5");

        // Action
        Contrato contrato = criar("loc1", "AM-2026-00123", "l", "t", "m",
                "Carlos", "Antonio", "Trator", INICIO, FIM, semCasas, umaCasa);

        // Assert
        assertEquals(new BigDecimal("850.00"), contrato.getValorTotal());
        assertEquals(new BigDecimal("0.50"), contrato.getValorCaucao());
    }
}
