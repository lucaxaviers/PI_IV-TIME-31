/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes OO Intraclasse (estados, atributos e categorias de metodos) da entidade VistoriaChecklist
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Testes Intraclasse da VistoriaChecklist (Issue #43), no padrao AAA.
 *
 * 1) ESTADOS: PENDENTE -> CONCLUIDA (todos conformes) | PENDENTE -> COM_PENDENCIAS (algum nao conforme)
 *    Comportamento padrao: construtor - [respondaItem]n - conclua - fim
 *    Variacoes: concluir sem responder tudo (erro), responder ou concluir depois de concluida (erro)
 *
 * 2) ATRIBUTOS: itens (copia profunda), horimetro e status
 *
 * 3) CATEGORIAS DE METODOS: inicializacao, computacao (respondaItem, conclua), consultas, predicativas e canonicos
 */
class VistoriaChecklistIntraclasseTest {

    private static final LocalDateTime DATA_FIXA = LocalDateTime.of(2026, 11, 10, 8, 0);

    private List<ItemChecklist> criarItens() throws Exception {
        List<ItemChecklist> itens = new ArrayList<>();
        itens.add(new ItemChecklist("Pneus e rodas"));
        itens.add(new ItemChecklist("Nivel de oleo"));
        itens.add(new ItemChecklist("Freios"));
        return itens;
    }

    // Vistoria nova (PENDENTE) com data fixa para o equals
    private VistoriaChecklist criarPendente() throws Exception {
        VistoriaChecklist vistoria = new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "locador1", criarItens());
        vistoria.setDataVistoria(DATA_FIXA);
        return vistoria;
    }

    private void responderTudoConforme(VistoriaChecklist vistoria) throws Exception {
        vistoria.respondaItem("Pneus e rodas", true, null);
        vistoria.respondaItem("Nivel de oleo", true, null);
        vistoria.respondaItem("Freios", true, null);
    }

    // =============== 1) ESTADOS ===============

    // CT-VEST-01 | estado inicial
    @Test
    void testVistoriaNovaNasceNoEstadoPendenteSemRespostas() throws Exception {
        // Arrange + Action
        VistoriaChecklist vistoria = criarPendente();

        // Assert
        assertEquals(StatusVistoria.PENDENTE, vistoria.getStatus());
        assertFalse(vistoria.isTodosRespondidos());
        assertFalse(vistoria.isConcluida());
        assertNull(vistoria.getHorimetro());
    }

    // CT-VEST-02 | sequencia: construtor - responde tudo conforme - conclua
    @Test
    void testSequenciaTudoConformeTerminaConcluida() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action
        responderTudoConforme(vistoria);
        assertTrue(vistoria.isTodosRespondidos());                  // ponto de verificacao antes da transicao
        vistoria.conclua(1200.5);

        // Assert
        assertEquals(StatusVistoria.CONCLUIDA, vistoria.getStatus());
        assertFalse(vistoria.temPendencias());
        assertEquals(1200.5, vistoria.getHorimetro());
    }

    // CT-VEST-03 | sequencia: construtor - responde com um nao conforme - conclua
    @Test
    void testSequenciaComItemNaoConformeTerminaComPendencias() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();
        vistoria.respondaItem("Pneus e rodas", true, null);
        vistoria.respondaItem("Nivel de oleo", true, null);
        vistoria.respondaItem("Freios", false, "freio dianteiro fraco");

        // Action
        vistoria.conclua(300.0);

        // Assert
        assertEquals(StatusVistoria.COM_PENDENCIAS, vistoria.getStatus());
        assertTrue(vistoria.temPendencias());
        assertTrue(vistoria.isConcluida());
    }

    // CT-VEST-04 | variacao: concluir sem responder todos os itens
    @Test
    void testConcluirSemResponderTudoDaErroEContinuaPendente() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();
        vistoria.respondaItem("Pneus e rodas", true, null);

        // Action
        Exception erro = assertThrows(Exception.class, () -> vistoria.conclua(100.0));

        // Assert
        assertEquals("Responda todos os itens do checklist", erro.getMessage());
        assertEquals(StatusVistoria.PENDENTE, vistoria.getStatus());
        assertNull(vistoria.getHorimetro());
    }

    // CT-VEST-05 | variacao: concluir duas vezes
    @Test
    void testConcluirDuasVezesDaErroEMantemOPrimeiroResultado() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();
        responderTudoConforme(vistoria);
        vistoria.conclua(100.0);

        // Action
        Exception erro = assertThrows(Exception.class, () -> vistoria.conclua(999.0));

        // Assert
        assertEquals("Vistoria ja concluida", erro.getMessage());
        assertEquals(100.0, vistoria.getHorimetro());
    }

    // CT-VEST-06 | variacao: responder item depois de concluida
    @Test
    void testResponderItemDepoisDeConcluidaDaErro() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();
        responderTudoConforme(vistoria);
        vistoria.conclua(100.0);

        // Action
        Exception erro = assertThrows(Exception.class, () -> vistoria.respondaItem("Freios", false, "quebrou"));

        // Assert
        assertEquals("Vistoria ja concluida", erro.getMessage());
        assertFalse(vistoria.temPendencias());
    }

    // CT-VEST-07 | variacao: item que nao existe
    @Test
    void testResponderItemInexistenteDaErro() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action
        Exception erro = assertThrows(Exception.class, () -> vistoria.respondaItem("Radio", true, null));

        // Assert
        assertEquals("Item nao existe no checklist: Radio", erro.getMessage());
    }

    // CT-VEST-08 | variacao: item nao conforme sem observacao e recusado e nao muda o estado
    @Test
    void testItemNaoConformeSemObservacaoNaoAlteraAVistoria() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action
        Exception erro = assertThrows(Exception.class, () -> vistoria.respondaItem("Freios", false, " "));

        // Assert
        assertEquals("Item nao conforme exige observacao", erro.getMessage());
        assertFalse(vistoria.isTodosRespondidos());
    }

    // =============== 2) ATRIBUTOS ===============

    // CT-VATR-01 | atributo itens: getItens devolve copia, mexer nela nao altera a vistoria
    @Test
    void testGetItensDevolveCopiaEMexerNelaNaoAlteraAVistoria() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action
        List<ItemChecklist> copia = vistoria.getItens();
        copia.get(0).responda(false, "alterado por fora");
        copia.clear();

        // Assert
        assertEquals(3, vistoria.getItens().size());
        assertFalse(vistoria.getItens().get(0).isRespondido());
    }

    // CT-VATR-02 | atributo itens: a lista recebida no construtor e copiada em profundidade
    @Test
    void testConstrutorFazCopiaProfundaDaListaRecebida() throws Exception {
        // Arrange
        List<ItemChecklist> itens = criarItens();
        VistoriaChecklist vistoria = new VistoriaChecklist("loc1", "maq1", TipoVistoria.RETIRADA, "r1", itens);

        // Action
        itens.get(0).responda(true, null);
        itens.clear();

        // Assert
        assertEquals(3, vistoria.getItens().size());
        assertFalse(vistoria.getItens().get(0).isRespondido());
    }

    // CT-VATR-03 | atributo itens: o construtor de copia isola a lista
    @Test
    void testConstrutorDeCopiaIsolaAListaDeItens() throws Exception {
        // Arrange
        VistoriaChecklist original = criarPendente();

        // Action
        VistoriaChecklist copia = new VistoriaChecklist(original);
        copia.respondaItem("Pneus e rodas", true, null);

        // Assert
        assertFalse(original.getItens().get(0).isRespondido());     // o original nao mudou
        assertTrue(copia.getItens().get(0).isRespondido());
        assertNotSame(original, copia);
    }

    // CT-VATR-04 | atributo status: so muda com conclua
    @Test
    void testStatusSoMudaQuandoAVistoriaEConcluida() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action
        responderTudoConforme(vistoria);
        StatusVistoria antes = vistoria.getStatus();
        vistoria.conclua(10.0);

        // Assert
        assertEquals(StatusVistoria.PENDENTE, antes);
        assertEquals(StatusVistoria.CONCLUIDA, vistoria.getStatus());
    }

    // =============== 3) CATEGORIAS DE METODOS ===============

    // CT-VMET-01 | inicializacao: construtor vazio
    @Test
    void testConstrutorVazioCriaObjetoSemDados() {
        // Arrange + Action
        VistoriaChecklist vistoria = new VistoriaChecklist();

        // Assert
        assertNull(vistoria.getId());
        assertNull(vistoria.getItens());
        assertNull(vistoria.getStatus());
    }

    // CT-VMET-02 | inicializacao: copia com modelo nulo
    @Test
    void testConstrutorDeCopiaRejeitaModeloNulo() {
        // Arrange
        VistoriaChecklist modelo = null;

        // Action
        Exception erro = assertThrows(Exception.class, () -> new VistoriaChecklist(modelo));

        // Assert
        assertEquals("Modelo ausente", erro.getMessage());
    }

    // CT-VMET-03 | inicializacao: clone independente
    @Test
    void testCloneGeraCopiaIgualEIndependente() throws Exception {
        // Arrange
        VistoriaChecklist original = criarPendente();

        // Action
        VistoriaChecklist clone = (VistoriaChecklist) original.clone();
        responderTudoConforme(clone);

        // Assert
        assertNotSame(original, clone);
        assertFalse(original.isTodosRespondidos());
        assertTrue(clone.isTodosRespondidos());
    }

    // CT-VMET-04 | consultas: getters
    @Test
    void testGettersDevolvemOsDadosDoConstrutor() throws Exception {
        // Arrange + Action
        VistoriaChecklist vistoria = criarPendente();

        // Assert
        assertEquals("loc1", vistoria.getLocacaoId());
        assertEquals("maq1", vistoria.getMaquinaId());
        assertEquals("locador1", vistoria.getResponsavelId());
        assertEquals(DATA_FIXA, vistoria.getDataVistoria());
    }

    // CT-VMET-05 | predicativas: temPendencias com e sem item nao conforme
    @Test
    void testTemPendenciasRetornaVerdadeiroEFalso() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action + Assert
        assertFalse(vistoria.temPendencias());
        vistoria.respondaItem("Freios", false, "fraco");
        assertTrue(vistoria.temPendencias());
    }

    // CT-VCAN-01 | equals e hashCode
    @Test
    void testEqualsEHashCodeParaMesmosDados() throws Exception {
        // Arrange
        VistoriaChecklist a = criarPendente();
        VistoriaChecklist b = criarPendente();

        // Action + Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.respondaItem("Freios", true, null);
        assertFalse(a.equals(b));                    // os itens fazem parte do equals
    }

    // CT-VCAN-02 | equals: os 4 passos
    @Test
    void testEqualsFalsoParaNuloOutraClasseEMesmaReferenciaVerdadeiro() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();

        // Action + Assert
        assertTrue(vistoria.equals(vistoria));
        assertFalse(vistoria.equals(null));
        assertFalse(vistoria.equals("vistoria"));
        assertTrue(vistoria.hashCode() >= 0);
    }

    // CT-VCAN-03 | compareTo pela data da vistoria
    @Test
    void testCompareToOrdenaPelaDataDaVistoria() throws Exception {
        // Arrange
        VistoriaChecklist antiga = criarPendente();
        VistoriaChecklist nova = criarPendente();
        nova.setDataVistoria(DATA_FIXA.plusHours(2));
        VistoriaChecklist semData = criarPendente();
        semData.setDataVistoria(null);

        // Action + Assert
        assertEquals(-666, antiga.compareTo(nova));
        assertEquals(666, nova.compareTo(antiga));
        assertEquals(0, antiga.compareTo(criarPendente()));
        assertEquals(-666, semData.compareTo(antiga));
        assertEquals(666, antiga.compareTo(semData));
        assertEquals(0, semData.compareTo(semData));
    }

    // CT-VCAN-04 | toString
    @Test
    void testToStringMostraTipoStatusEHorimetro() throws Exception {
        // Arrange
        VistoriaChecklist vistoria = criarPendente();
        responderTudoConforme(vistoria);

        // Action
        String antes = vistoria.toString();
        vistoria.conclua(450.5);
        String depois = vistoria.toString();

        // Assert
        assertTrue(antes.contains("RETIRADA"));
        assertTrue(antes.contains("PENDENTE"));
        assertTrue(depois.contains("CONCLUIDA"));
        assertTrue(depois.contains("450.5"));
    }
}
