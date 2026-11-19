/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes de Caixa-Preta (Equivalencia e BVA) e Intraclasse do MaquinaCadastroRequest
*/

package com.alugamaquina.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Requisito de teste RT-08: o cadastro de maquina so aceita dados validos.
 *
 * Particionamento de Equivalencia
 *   tituloAnuncio | V1 de 5 a 100 caracteres | I1 nulo, I2 vazio ou so espacos, I3 curto demais, I4 longo demais
 *   tipo          | V1 codigo da lista       | I1 nulo ou vazio, I2 fora da lista
 *   municipio     | V1 codigo da lista       | I1 nulo ou vazio, I2 fora da lista
 *   valorDiaria   | V1 de 0,01 a 50.000,00   | I1 nulo, I2 zero, I3 negativo, I4 acima de 50.000,00
 *
 * BVA: titulo com 4, 5, 100 e 101 caracteres; valor com 0,00, 0,01, 50.000,00 e 50.000,01.
 *
 * Intraclasse (categorias de metodos): inicializacao (construtores, copia, clone), setup (setters),
 * consultas (getters), predicativas (isValido e validacoes estaticas) e canonicos (equals, hashCode, compareTo, toString).
 */
class MaquinaCadastroRequestTest {

    private MaquinaCadastroRequest criarValido() throws Exception {
        return new MaquinaCadastroRequest("Trator John Deere 6115J", "trator", "paulinia", new BigDecimal("850"));
    }

    private String texto(int tamanho) {
        return "a".repeat(tamanho);
    }

    // ---------- Equivalencia: construtor ----------

    // CT-REQ-01 | V1 de todas as entradas
    @Test
    void testConstrutorComClassesValidasCriaOPedido() throws Exception {
        // Arrange - massa valida

        // Action
        MaquinaCadastroRequest pedido = criarValido();

        // Assert
        assertEquals("Trator John Deere 6115J", pedido.getTituloAnuncio());
        assertEquals("trator", pedido.getTipoEquipamento());
        assertEquals("paulinia", pedido.getMunicipio());
        assertTrue(pedido.isValido());
    }

    // CT-REQ-02 | construtor apara o titulo, normaliza os codigos e deixa o valor com 2 casas
    @Test
    void testConstrutorNormalizaOsDados() throws Exception {
        // Arrange
        String titulo = "  Colheitadeira Case 2022  ";

        // Action
        MaquinaCadastroRequest pedido = new MaquinaCadastroRequest(titulo, " COLHEITADEIRA ", " Campinas ", new BigDecimal("2400"));

        // Assert
        assertEquals("Colheitadeira Case 2022", pedido.getTituloAnuncio());
        assertEquals("colheitadeira", pedido.getTipoEquipamento());
        assertEquals("campinas", pedido.getMunicipio());
        assertEquals(new BigDecimal("2400.00"), pedido.getValorDiaria());
    }

    // CT-REQ-03 | I1 e I2: titulo nulo, vazio ou so espacos
    @ParameterizedTest(name = "CT-REQ-03 titulo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"     "})
    void testConstrutorRejeitaTituloVazio(String titulo) {
        // Arrange - titulo invalido

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new MaquinaCadastroRequest(titulo, "trator", "paulinia", new BigDecimal("850")));

        // Assert
        assertEquals("Titulo do anuncio invalido", erro.getMessage());
    }

    // CT-REQ-04 | I1 e I2: tipo nulo, vazio ou fora da lista
    @ParameterizedTest(name = "CT-REQ-04 tipo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"caminhao", "tratores"})
    void testConstrutorRejeitaTipoInvalido(String tipo) {
        // Arrange - tipo invalido

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new MaquinaCadastroRequest("Trator John Deere", tipo, "paulinia", new BigDecimal("850")));

        // Assert
        assertEquals("Tipo de equipamento invalido", erro.getMessage());
    }

    // CT-REQ-05 | I1 e I2: municipio nulo, vazio ou fora da lista
    @ParameterizedTest(name = "CT-REQ-05 municipio=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"sao-paulo", "Paulínia / SP"})
    void testConstrutorRejeitaMunicipioInvalido(String municipio) {
        // Arrange - municipio invalido

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new MaquinaCadastroRequest("Trator John Deere", "trator", municipio, new BigDecimal("850")));

        // Assert
        assertEquals("Municipio invalido", erro.getMessage());
    }

    // CT-REQ-06 | I1, I2 e I3: valor nulo, zero ou negativo
    @ParameterizedTest(name = "CT-REQ-06 valor={0}")
    @CsvSource(value = {"NULL", "0", "-1", "-850.00"}, nullValues = "NULL")
    void testConstrutorRejeitaValorNuloZeroOuNegativo(BigDecimal valor) {
        // Arrange - valor invalido

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new MaquinaCadastroRequest("Trator John Deere", "trator", "paulinia", valor));

        // Assert
        assertEquals("Valor da diaria invalido", erro.getMessage());
    }

    // ---------- BVA ----------

    // CT-BVA-20 | tamanho do titulo: 4 (abaixo), 5 (limite), 100 (limite) e 101 (acima)
    @ParameterizedTest(name = "CT-BVA-20 titulo com {0} caracteres -> valido={1}")
    @CsvSource({"4, false", "5, true", "6, true", "99, true", "100, true", "101, false"})
    void testBvaDoTamanhoDoTitulo(int tamanho, boolean esperado) {
        // Arrange
        String titulo = texto(tamanho);

        // Action
        boolean valido = MaquinaCadastroRequest.isTituloValido(titulo);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-21 | espacos das pontas nao contam no tamanho do titulo
    @Test
    void testTituloComEspacosNasPontasConsideraSoOTextoUtil() {
        // Arrange
        String quatroUteis = "  abcd  ";
        String cincoUteis = "  abcde  ";

        // Action + Assert
        assertFalse(MaquinaCadastroRequest.isTituloValido(quatroUteis));
        assertTrue(MaquinaCadastroRequest.isTituloValido(cincoUteis));
    }

    // CT-BVA-22 | valor da diaria: 0,00 (invalido), 0,01 (limite), 50.000,00 (limite) e 50.000,01 (acima)
    @ParameterizedTest(name = "CT-BVA-22 valor={0} -> valido={1}")
    @CsvSource({"0.00, false", "0.01, true", "0.02, true", "49999.99, true", "50000.00, true", "50000.01, false"})
    void testBvaDoValorDaDiaria(String valor, boolean esperado) {
        // Arrange
        BigDecimal diaria = new BigDecimal(valor);

        // Action
        boolean valido = MaquinaCadastroRequest.isValorDiariaValido(diaria);

        // Assert
        assertEquals(esperado, valido);
    }

    // ---------- Intraclasse: inicializacao e setup ----------

    // CT-REQ-07 | construtor vazio deixa tudo nulo e o pedido invalido
    @Test
    void testConstrutorVazioCriaPedidoSemDadosENaoValido() {
        // Arrange + Action
        MaquinaCadastroRequest pedido = new MaquinaCadastroRequest();

        // Assert
        assertNull(pedido.getTituloAnuncio());
        assertNull(pedido.getValorDiaria());
        assertFalse(pedido.isValido());
    }

    // CT-REQ-08 | setters (usados pelo JSON) preenchem o pedido e o isValido passa a ser verdadeiro
    @Test
    void testSettersPreenchemOPedidoELeitoresDevolvemOsDados() {
        // Arrange
        MaquinaCadastroRequest pedido = new MaquinaCadastroRequest();

        // Action
        pedido.setTituloAnuncio("Pulverizador Jacto 2020");
        pedido.setTipoEquipamento("pulverizador");
        pedido.setMunicipio("holambra");
        pedido.setValorDiaria(new BigDecimal("1300.00"));

        // Assert
        assertEquals("Pulverizador Jacto 2020", pedido.getTituloAnuncio());
        assertEquals("pulverizador", pedido.getTipoEquipamento());
        assertEquals("holambra", pedido.getMunicipio());
        assertEquals(new BigDecimal("1300.00"), pedido.getValorDiaria());
        assertTrue(pedido.isValido());
    }

    // CT-REQ-09 | isValido e falso se qualquer campo estiver invalido
    @Test
    void testIsValidoFicaFalsoQuandoUmCampoEInvalido() {
        // Arrange
        MaquinaCadastroRequest pedido = new MaquinaCadastroRequest();
        pedido.setTituloAnuncio("Trator Massey 2019");
        pedido.setTipoEquipamento("trator");
        pedido.setMunicipio("campinas");
        pedido.setValorDiaria(new BigDecimal("700"));
        assertTrue(pedido.isValido());

        // Action
        pedido.setValorDiaria(BigDecimal.ZERO);

        // Assert
        assertFalse(pedido.isValido());
    }

    // CT-REQ-10 | construtor de copia copia os campos
    @Test
    void testConstrutorDeCopiaCopiaTodosOsCampos() throws Exception {
        // Arrange
        MaquinaCadastroRequest original = criarValido();

        // Action
        MaquinaCadastroRequest copia = new MaquinaCadastroRequest(original);

        // Assert
        assertEquals(original, copia);
        assertNotSame(original, copia);
    }

    // CT-REQ-11 | construtor de copia com modelo nulo
    @Test
    void testConstrutorDeCopiaRejeitaModeloNulo() {
        // Arrange
        MaquinaCadastroRequest modelo = null;

        // Action
        Exception erro = assertThrows(Exception.class, () -> new MaquinaCadastroRequest(modelo));

        // Assert
        assertEquals("Modelo ausente", erro.getMessage());
    }

    // CT-REQ-12 | clone gera copia independente
    @Test
    void testCloneGeraCopiaIgualEIndependente() throws Exception {
        // Arrange
        MaquinaCadastroRequest original = criarValido();

        // Action
        MaquinaCadastroRequest clone = (MaquinaCadastroRequest) original.clone();
        clone.setTituloAnuncio("Outro titulo");

        // Assert
        assertNotSame(original, clone);
        assertEquals("Trator John Deere 6115J", original.getTituloAnuncio());
        assertEquals("Outro titulo", clone.getTituloAnuncio());
    }

    // ---------- Canonicos ----------

    // CT-CAN-10 | equals verdadeiro e hashCode igual para os mesmos dados
    @Test
    void testEqualsEHashCodeParaMesmosDados() throws Exception {
        // Arrange
        MaquinaCadastroRequest a = criarValido();
        MaquinaCadastroRequest b = criarValido();

        // Action
        boolean iguais = a.equals(b);

        // Assert
        assertTrue(iguais);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // CT-CAN-11 | equals: os 4 passos (mesma referencia, nulo, outra classe, campo diferente)
    @Test
    void testEqualsFalsoParaNuloOutraClasseECampoDiferente() throws Exception {
        // Arrange
        MaquinaCadastroRequest pedido = criarValido();
        MaquinaCadastroRequest diferente = criarValido();
        diferente.setMunicipio("campinas");

        // Action + Assert
        assertTrue(pedido.equals(pedido));           // passo 1: mesmo endereco
        assertFalse(pedido.equals(null));            // passo 2: nulo
        assertFalse(pedido.equals("Trator"));        // passo 3: outra classe
        assertFalse(pedido.equals(diferente));       // passo 4: campo diferente
    }

    // CT-CAN-12 | hashCode diferente para pedidos diferentes e nunca negativo
    @Test
    void testHashCodeDiferenteParaPedidosDiferentesENaoNegativo() throws Exception {
        // Arrange
        MaquinaCadastroRequest a = criarValido();
        MaquinaCadastroRequest b = criarValido();
        b.setTituloAnuncio("Trator Valtra BH180");

        // Action
        int hashA = a.hashCode();
        int hashB = b.hashCode();

        // Assert
        assertTrue(hashA >= 0 && hashB >= 0);
        assertTrue(hashA != hashB);
        assertTrue(new MaquinaCadastroRequest().hashCode() >= 0);
    }

    // CT-CAN-13 | compareTo ordena pelo titulo e depois pelo valor
    @Test
    void testCompareToOrdenaPeloTituloEDepoisPeloValor() throws Exception {
        // Arrange
        MaquinaCadastroRequest a = new MaquinaCadastroRequest("Arado reversivel", "implemento", "sumare", new BigDecimal("300"));
        MaquinaCadastroRequest b = new MaquinaCadastroRequest("Trator Valtra", "trator", "sumare", new BigDecimal("700"));
        MaquinaCadastroRequest mesmoTituloMaisCaro = new MaquinaCadastroRequest("Arado reversivel", "implemento", "sumare", new BigDecimal("400"));

        // Action + Assert
        assertEquals(-666, a.compareTo(b));
        assertEquals(666, b.compareTo(a));
        assertEquals(-666, a.compareTo(mesmoTituloMaisCaro));
        assertEquals(666, mesmoTituloMaisCaro.compareTo(a));
        assertEquals(0, a.compareTo((MaquinaCadastroRequest) a.clone()));
    }

    // CT-CAN-14 | compareTo com campos nulos
    @Test
    void testCompareToTrataTituloEValorNulos() throws Exception {
        // Arrange
        MaquinaCadastroRequest vazio = new MaquinaCadastroRequest();
        MaquinaCadastroRequest comTitulo = criarValido();
        MaquinaCadastroRequest semValor = new MaquinaCadastroRequest();
        semValor.setTituloAnuncio("Trator John Deere 6115J");

        // Action + Assert
        assertEquals(0, vazio.compareTo(new MaquinaCadastroRequest()));
        assertEquals(-666, vazio.compareTo(comTitulo));
        assertEquals(666, comTitulo.compareTo(vazio));
        assertEquals(-666, semValor.compareTo(comTitulo));        // mesmo titulo, valor nulo vem primeiro
        assertEquals(666, comTitulo.compareTo(semValor));
    }

    // CT-CAN-15 | toString mostra titulo, tipo, municipio e valor
    @Test
    void testToStringMostraOsDadosDoPedido() throws Exception {
        // Arrange
        MaquinaCadastroRequest pedido = criarValido();

        // Action
        String texto = pedido.toString();

        // Assert
        assertTrue(texto.contains("Trator John Deere 6115J"));
        assertTrue(texto.contains("paulinia"));
        assertTrue(texto.contains("850.00"));
    }
}
