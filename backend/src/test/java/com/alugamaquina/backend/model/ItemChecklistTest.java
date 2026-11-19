/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes do ItemChecklist: Particionamento de Equivalencia, estados e metodos canonicos
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Requisito de teste RT-04 (Issue #43): cada item do checklist so e valido com descricao,
 * e item nao conforme exige observacao.
 *
 * Particionamento de Equivalencia
 *   descricao  | V1 texto com conteudo         | I1 nula, I2 vazia ou so espacos
 *   resposta   | V1 conforme (com ou sem obs.) | I1 nao conforme sem observacao (nula, vazia ou espacos)
 *              | V2 nao conforme com observacao
 *
 * Estados do item: NAO_RESPONDIDO -> CONFORME | NAO_CONFORME (responda); responder de novo troca a resposta.
 */
class ItemChecklistTest {

    private ItemChecklist criarItem() throws Exception {
        return new ItemChecklist("Pneus e rodas sem avarias");
    }

    // ---------- Equivalencia: descricao ----------

    // CT-ITM-01 | V1: descricao valida e aparada
    @Test
    void testConstrutorGuardaADescricaoSemEspacosNasPontas() throws Exception {
        // Arrange
        String descricao = "  Nivel de oleo  ";

        // Action
        ItemChecklist item = new ItemChecklist(descricao);

        // Assert
        assertEquals("Nivel de oleo", item.getDescricao());
    }

    // CT-ITM-02 | I1 e I2: descricao nula, vazia ou so espacos
    @ParameterizedTest(name = "CT-ITM-02 descricao=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testConstrutorRejeitaDescricaoInvalida(String descricao) {
        // Arrange - descricao invalida

        // Action
        Exception erro = assertThrows(Exception.class, () -> new ItemChecklist(descricao));

        // Assert
        assertEquals("Descricao do item ausente", erro.getMessage());
    }

    // CT-ITM-03 | setDescricao valida como o construtor
    @Test
    void testSetDescricaoValidaEMantemOValorAnteriorEmCasoDeErro() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();

        // Action
        Exception erro = assertThrows(Exception.class, () -> item.setDescricao("  "));

        // Assert
        assertEquals("Descricao do item ausente", erro.getMessage());
        assertEquals("Pneus e rodas sem avarias", item.getDescricao());
    }

    // ---------- Estados: responder o item ----------

    // CT-ITM-04 | estado inicial: nao respondido
    @Test
    void testItemNovoNasceNaoRespondido() throws Exception {
        // Arrange + Action
        ItemChecklist item = criarItem();

        // Assert
        assertFalse(item.isRespondido());
        assertFalse(item.isNaoConforme());
        assertNull(item.getConforme());
    }

    // CT-ITM-05 | V1: conforme sem observacao
    @Test
    void testResponderConformeSemObservacaoDeixaOItemConforme() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();

        // Action
        item.responda(true, null);

        // Assert
        assertTrue(item.isRespondido());
        assertFalse(item.isNaoConforme());
        assertNull(item.getObservacao());
    }

    // CT-ITM-06 | V1: conforme com observacao, a observacao e guardada
    @Test
    void testResponderConformeComObservacaoGuardaAObservacao() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();

        // Action
        item.responda(true, "  desgaste normal  ");

        // Assert
        assertEquals("desgaste normal", item.getObservacao());
    }

    // CT-ITM-07 | V2: nao conforme com observacao
    @Test
    void testResponderNaoConformeComObservacaoDeixaOItemNaoConforme() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();

        // Action
        item.responda(false, "pneu dianteiro furado");

        // Assert
        assertTrue(item.isNaoConforme());
        assertEquals("pneu dianteiro furado", item.getObservacao());
    }

    // CT-ITM-08 | I1: nao conforme sem observacao (nula, vazia ou espacos)
    @ParameterizedTest(name = "CT-ITM-08 observacao=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void testResponderNaoConformeSemObservacaoDaErroENaoAlteraOItem(String observacao) throws Exception {
        // Arrange
        ItemChecklist item = criarItem();

        // Action
        Exception erro = assertThrows(Exception.class, () -> item.responda(false, observacao));

        // Assert
        assertEquals("Item nao conforme exige observacao", erro.getMessage());
        assertFalse(item.isRespondido());
    }

    // CT-ITM-09 | transicao: responder de novo troca a resposta
    @Test
    void testResponderDeNovoTrocaDeNaoConformeParaConforme() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();
        item.responda(false, "arranhao");

        // Action
        item.responda(true, null);

        // Assert
        assertFalse(item.isNaoConforme());
        assertNull(item.getObservacao());
    }

    // ---------- Metodos canonicos ----------

    // CT-ITM-10 | equals e hashCode coerentes
    @Test
    void testEqualsEHashCodeParaMesmosDados() throws Exception {
        // Arrange
        ItemChecklist a = criarItem();
        ItemChecklist b = criarItem();
        a.responda(false, "risco");
        b.responda(false, "risco");

        // Action + Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // CT-ITM-11 | equals falso: nulo, outra classe, outro campo; verdadeiro para a mesma referencia
    @Test
    void testEqualsFalsoParaNuloOutraClasseEOutroCampo() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();
        ItemChecklist respondido = criarItem();
        respondido.responda(true, null);

        // Action + Assert
        assertTrue(item.equals(item));
        assertFalse(item.equals(null));
        assertFalse(item.equals("Pneus"));
        assertFalse(item.equals(respondido));
        assertTrue(item.hashCode() >= 0);
    }

    // CT-ITM-12 | compareTo ordena por descricao
    @Test
    void testCompareToOrdenaPelaDescricao() throws Exception {
        // Arrange
        ItemChecklist a = new ItemChecklist("Freios");
        ItemChecklist b = new ItemChecklist("Luzes");

        // Action + Assert
        assertEquals(-666, a.compareTo(b));
        assertEquals(666, b.compareTo(a));
        assertEquals(0, a.compareTo(new ItemChecklist("Freios")));
        assertEquals(0, new ItemChecklist().compareTo(new ItemChecklist()));
        assertEquals(-666, new ItemChecklist().compareTo(a));
        assertEquals(666, a.compareTo(new ItemChecklist()));
    }

    // CT-ITM-13 | toString mostra a situacao do item
    @Test
    void testToStringMostraASituacaoDoItem() throws Exception {
        // Arrange
        ItemChecklist item = criarItem();

        // Action
        String antes = item.toString();
        item.responda(false, "furado");
        String depois = item.toString();

        // Assert
        assertTrue(antes.contains("nao respondido"));
        assertTrue(depois.contains("NAO conforme"));
        assertTrue(depois.contains("furado"));
    }

    // CT-ITM-14 | construtor de copia e clone
    @Test
    void testCopiaECloneGeramObjetosIgualEIndependentes() throws Exception {
        // Arrange
        ItemChecklist original = criarItem();
        original.responda(true, null);

        // Action
        ItemChecklist copia = new ItemChecklist(original);
        ItemChecklist clone = (ItemChecklist) original.clone();
        clone.responda(false, "mudou");

        // Assert
        assertEquals(original, copia);
        assertNotSame(original, copia);
        assertTrue(original.getConforme());             // mexer no clone nao altera o original
        assertFalse(clone.getConforme());
    }

    // CT-ITM-15 | construtor de copia com modelo nulo
    @Test
    void testCopiaRejeitaModeloNulo() {
        // Arrange
        ItemChecklist modelo = null;

        // Action
        Exception erro = assertThrows(Exception.class, () -> new ItemChecklist(modelo));

        // Assert
        assertEquals("Modelo ausente", erro.getMessage());
    }
}
