/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes dos enums MunicipioAtendido e TipoEquipamento do cadastro de maquinas (Particionamento de Equivalencia)
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Requisito de teste RT-07: o codigo vindo do formulario precisa virar o municipio e o tipo corretos.
 *
 * Particionamento de Equivalencia (codigo recebido)
 *   V1 codigo da lista, em minusculas | V2 codigo da lista com maiusculas ou espacos nas pontas
 *   I1 nulo | I2 vazio ou so espacos | I3 codigo que nao esta na lista
 *
 * Cada teste faz uma coisa so e segue o padrao AAA (Arrange, Action, Assert).
 */
class CadastroEnumsTest {

    // ---------- MunicipioAtendido ----------

    // CT-ENM-01 | V1: todo codigo da lista encontra o proprio municipio
    @ParameterizedTest(name = "CT-ENM-01 municipio={0}")
    @EnumSource(MunicipioAtendido.class)
    void testBuscarPorCodigoEncontraCadaMunicipioDaLista(MunicipioAtendido esperado) {
        // Arrange
        String codigo = esperado.getCodigo();

        // Action
        MunicipioAtendido encontrado = MunicipioAtendido.buscarPorCodigo(codigo);

        // Assert
        assertSame(esperado, encontrado);
        assertTrue(MunicipioAtendido.isCodigoValido(codigo));
    }

    // CT-ENM-02 | V2: maiusculas e espacos nas pontas sao aceitos
    @Test
    void testMunicipioAceitaMaiusculasEEspacosNasPontas() {
        // Arrange
        String codigo = "  PAULINIA  ";

        // Action
        MunicipioAtendido encontrado = MunicipioAtendido.buscarPorCodigo(codigo);

        // Assert
        assertSame(MunicipioAtendido.PAULINIA, encontrado);
    }

    // CT-ENM-03 | I1, I2 e I3: codigos invalidos nao encontram municipio
    @ParameterizedTest(name = "CT-ENM-03 codigo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "sao-paulo", "paulinia / sp", "Paulínia"})
    void testMunicipioInvalidoNaoEncontraNada(String codigo) {
        // Arrange - codigo fora da lista

        // Action
        MunicipioAtendido encontrado = MunicipioAtendido.buscarPorCodigo(codigo);

        // Assert
        assertNull(encontrado);
        assertFalse(MunicipioAtendido.isCodigoValido(codigo));
    }

    // CT-ENM-04 | a lista tem os 7 municipios do formulario
    @Test
    void testListaTemOsSeteMunicipiosDoFormulario() {
        // Arrange + Action
        int quantidade = MunicipioAtendido.values().length;

        // Assert
        assertEquals(7, quantidade);
    }

    // CT-ENM-05 | nome de exibicao e coordenadas conferem com os dados iniciais do banco
    @Test
    void testPauliniaTemNomeECoordenadasDoCatalogo() {
        // Arrange
        MunicipioAtendido paulinia = MunicipioAtendido.PAULINIA;

        // Action + Assert
        assertEquals("Paulínia / SP", paulinia.getNome());
        assertEquals(-47.1100, paulinia.getLongitude());
        assertEquals(-22.7100, paulinia.getLatitude());
    }

    // CT-ENM-06 | todas as coordenadas ficam na regiao de Campinas (longitude e latitude validas)
    @ParameterizedTest(name = "CT-ENM-06 municipio={0}")
    @EnumSource(MunicipioAtendido.class)
    void testCoordenadasDeCadaMunicipioSaoValidasEDaRegiao(MunicipioAtendido municipio) {
        // Arrange - limites da regiao de Campinas / SP

        // Action + Assert
        assertTrue(municipio.getLongitude() > -47.5 && municipio.getLongitude() < -46.8);
        assertTrue(municipio.getLatitude() > -23.1 && municipio.getLatitude() < -22.4);
        assertTrue(municipio.getNome().endsWith(" / SP"));
    }

    // ---------- TipoEquipamento ----------

    // CT-TIP-01 | V1: todo codigo da lista encontra o proprio tipo
    @ParameterizedTest(name = "CT-TIP-01 tipo={0}")
    @EnumSource(TipoEquipamento.class)
    void testBuscarPorCodigoEncontraCadaTipoDaLista(TipoEquipamento esperado) {
        // Arrange
        String codigo = esperado.getCodigo();

        // Action
        TipoEquipamento encontrado = TipoEquipamento.buscarPorCodigo(codigo);

        // Assert
        assertSame(esperado, encontrado);
        assertTrue(TipoEquipamento.isCodigoValido(codigo));
    }

    // CT-TIP-02 | V2: maiusculas e espacos nas pontas sao aceitos
    @Test
    void testTipoAceitaMaiusculasEEspacosNasPontas() {
        // Arrange
        String codigo = " Colheitadeira ";

        // Action
        TipoEquipamento encontrado = TipoEquipamento.buscarPorCodigo(codigo);

        // Assert
        assertSame(TipoEquipamento.COLHEITADEIRA, encontrado);
    }

    // CT-TIP-03 | I1, I2 e I3: codigos invalidos nao encontram tipo (inclusive o plural do catalogo)
    @ParameterizedTest(name = "CT-TIP-03 codigo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "caminhao", "tratores"})
    void testTipoInvalidoNaoEncontraNada(String codigo) {
        // Arrange - codigo fora da lista

        // Action
        TipoEquipamento encontrado = TipoEquipamento.buscarPorCodigo(codigo);

        // Assert
        assertNull(encontrado);
        assertFalse(TipoEquipamento.isCodigoValido(codigo));
    }

    // CT-TIP-04 | o codigo do formulario vira a chave plural que o catalogo usa
    @Test
    void testChaveDoCatalogoEOPluralDoCodigo() {
        // Arrange + Action + Assert
        assertEquals("tratores", TipoEquipamento.TRATOR.getChaveCatalogo());
        assertEquals("colheitadeiras", TipoEquipamento.COLHEITADEIRA.getChaveCatalogo());
        assertEquals("pulverizadores", TipoEquipamento.PULVERIZADOR.getChaveCatalogo());
        assertEquals("plantadeiras", TipoEquipamento.PLANTADEIRA.getChaveCatalogo());
        assertEquals("implementos", TipoEquipamento.IMPLEMENTO.getChaveCatalogo());
    }
}
