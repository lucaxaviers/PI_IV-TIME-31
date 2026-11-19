/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes de Caixa-Preta (Particionamento de Equivalencia e BVA) da entidade VistoriaChecklist
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Requisito de teste RT-05 (Issue #43): a vistoria so pode ser criada com dados validos
 * e so pode ser concluida com horimetro valido e todos os itens respondidos.
 *
 * Particionamento de Equivalencia
 *   textos (locacao, maquina, responsavel) | V1 texto com conteudo | I1 nulo, I2 vazio ou so espacos
 *   tipo                                   | V1 RETIRADA ou DEVOLUCAO | I1 nulo
 *   itens                                  | V1 lista com itens      | I1 nula, I2 vazia, I3 com item nulo
 *   horimetro                              | V1 zero ou maior        | I1 negativo, I2 nulo, I3 NaN ou infinito
 *
 * BVA do horimetro: limite = 0 (testa -0,1 / 0 / 0,1).
 */
class VistoriaChecklistBvaTest {

    private List<ItemChecklist> criarItens() throws Exception {
        List<ItemChecklist> itens = new ArrayList<>();
        itens.add(new ItemChecklist("Pneus e rodas"));
        itens.add(new ItemChecklist("Nivel de oleo"));
        return itens;
    }

    private VistoriaChecklist criarVistoria() throws Exception {
        return new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "locador1", criarItens());
    }

    // ---------- Equivalencia: criacao ----------

    // CT-VIS-01 | V1 de todas as entradas
    @Test
    void testConstrutorComClassesValidasCriaVistoriaPendente() throws Exception {
        // Arrange - massa valida

        // Action
        VistoriaChecklist vistoria = criarVistoria();

        // Assert
        assertEquals(StatusVistoria.PENDENTE, vistoria.getStatus());
        assertEquals(2, vistoria.getItens().size());
        assertEquals(TipoVistoria.RETIRADA, vistoria.getTipo());
    }

    // CT-VIS-02 | V1: tipo DEVOLUCAO tambem e valido
    @Test
    void testConstrutorAceitaTipoDevolucao() throws Exception {
        // Arrange
        TipoVistoria tipo = TipoVistoria.DEVOLUCAO;

        // Action
        VistoriaChecklist vistoria = new VistoriaChecklist("loc1", "maq1", tipo, "locador1", criarItens());

        // Assert
        assertEquals(TipoVistoria.DEVOLUCAO, vistoria.getTipo());
    }

    // CT-VIS-03 | I1 e I2: locacao vazia
    @ParameterizedTest(name = "CT-VIS-03 locacaoId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testConstrutorRejeitaLocacaoInvalida(String locacaoId) {
        // Arrange - locacao invalida

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new VistoriaChecklist(locacaoId, "maq1", TipoVistoria.RETIRADA, "r1", criarItens()));

        // Assert
        assertEquals("Locacao ausente", erro.getMessage());
    }

    // CT-VIS-04 | I1 e I2: maquina vazia
    @ParameterizedTest(name = "CT-VIS-04 maquinaId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testConstrutorRejeitaMaquinaInvalida(String maquinaId) {
        // Arrange - maquina invalida

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new VistoriaChecklist("loc1", maquinaId, TipoVistoria.RETIRADA, "r1", criarItens()));

        // Assert
        assertEquals("Maquina ausente", erro.getMessage());
    }

    // CT-VIS-05 | I1 e I2: responsavel vazio
    @ParameterizedTest(name = "CT-VIS-05 responsavelId=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testConstrutorRejeitaResponsavelInvalido(String responsavelId) {
        // Arrange - responsavel invalido

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, responsavelId, criarItens()));

        // Assert
        assertEquals("Responsavel ausente", erro.getMessage());
    }

    // CT-VIS-06 | I1: tipo nulo
    @Test
    void testConstrutorRejeitaTipoNulo() {
        // Arrange - tipo nulo

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new VistoriaChecklist("loc1", "maq1", null, "r1", criarItens()));

        // Assert
        assertEquals("Tipo da vistoria ausente", erro.getMessage());
    }

    // CT-VIS-07 | I1 e I2: itens nulos ou lista vazia
    @Test
    void testConstrutorRejeitaListaDeItensNulaOuVazia() {
        // Arrange - lista nula e lista vazia

        // Action
        Exception nula = assertThrows(Exception.class,
                () -> new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "r1", null));
        Exception vazia = assertThrows(Exception.class,
                () -> new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "r1", new ArrayList<>()));

        // Assert
        assertEquals("Itens do checklist ausentes", nula.getMessage());
        assertEquals("Itens do checklist ausentes", vazia.getMessage());
    }

    // CT-VIS-08 | I3: lista com um item nulo
    @Test
    void testConstrutorRejeitaListaComItemNulo() throws Exception {
        // Arrange
        List<ItemChecklist> itens = criarItens();
        itens.add(null);

        // Action
        Exception erro = assertThrows(Exception.class,
                () -> new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "r1", itens));

        // Assert
        assertEquals("Item do checklist ausente", erro.getMessage());
    }

    // ---------- BVA do horimetro ----------

    // CT-BVA-10 | limite = 0
    @ParameterizedTest(name = "CT-BVA-10 horimetro={0} -> valido={1}")
    @CsvSource({"-0.1, false", "0.0, true", "0.1, true", "1200.5, true"})
    void testBvaDoHorimetro(double horimetro, boolean esperado) {
        // Arrange - horimetro no limite, abaixo e acima

        // Action
        boolean valido = VistoriaChecklist.isHorimetroValido(horimetro);

        // Assert
        assertEquals(esperado, valido);
    }

    // CT-BVA-11 | I2 e I3: nulo, NaN e infinito
    @Test
    void testHorimetroNuloNaNEInfinitoSaoInvalidos() {
        // Arrange - valores especiais

        // Action + Assert
        assertFalse(VistoriaChecklist.isHorimetroValido(null));
        assertFalse(VistoriaChecklist.isHorimetroValido(Double.NaN));
        assertFalse(VistoriaChecklist.isHorimetroValido(Double.POSITIVE_INFINITY));
        assertFalse(VistoriaChecklist.isHorimetroValido(Double.NEGATIVE_INFINITY));
    }

    // CT-BVA-12 | concluir com horimetro negativo nao conclui
    @Test
    void testConcluirComHorimetroNegativoDaErroEMantemPendente() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarVistoria();
        vistoria.respondaItem("Pneus e rodas", true, null);
        vistoria.respondaItem("Nivel de oleo", true, null);

        // Action
        Exception erro = assertThrows(Exception.class, () -> vistoria.conclua(-0.1));

        // Assert
        assertEquals("Horimetro invalido", erro.getMessage());
        assertEquals(StatusVistoria.PENDENTE, vistoria.getStatus());
    }

    // CT-BVA-13 | concluir com horimetro zero (limite) funciona
    @Test
    void testConcluirComHorimetroZeroConclui() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarVistoria();
        vistoria.respondaItem("Pneus e rodas", true, null);
        vistoria.respondaItem("Nivel de oleo", true, null);

        // Action
        vistoria.conclua(0.0);

        // Assert
        assertEquals(StatusVistoria.CONCLUIDA, vistoria.getStatus());
        assertEquals(0.0, vistoria.getHorimetro());
        assertTrue(vistoria.isConcluida());
    }

    // CT-VIS-09 | textos validos sao aparados
    @Test
    void testConstrutorApagaEspacosDosIds() throws Exception {
        // Arrange
        String locacaoComEspacos = "  loc1  ";

        // Action
        VistoriaChecklist vistoria = new VistoriaChecklist(locacaoComEspacos, " maq1 ", TipoVistoria.RETIRADA,
                " locador1 ", criarItens());

        // Assert
        assertEquals("loc1", vistoria.getLocacaoId());
        assertEquals("maq1", vistoria.getMaquinaId());
        assertEquals("locador1", vistoria.getResponsavelId());
    }
}
