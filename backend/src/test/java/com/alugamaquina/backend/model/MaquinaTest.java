/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Testes unitarios JUnit 5 dos metodos canonicos da entidade Maquina (Issue #46)
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

class MaquinaTest {

    // Cria uma maquina de exemplo para os testes (longitude, latitude)
    private Maquina criarMaquina(String titulo, BigDecimal valorDiaria) {
        return new Maquina(titulo, "trator", "Paulinia", valorDiaria, new GeoJsonPoint(-47.1, -22.9));
    }

    @Test
    void equalsDeveSerVerdadeiroParaMesmosDados() {
        // Arrange
        Maquina a = criarMaquina("Trator Vermelho", new BigDecimal("500"));
        Maquina b = criarMaquina("Trator Vermelho", new BigDecimal("500"));

        // Act + Assert
        assertEquals(a, b);
    }

    @Test
    void equalsDeveSerFalsoParaValorDiferenteNuloOuOutraClasse() {
        // Arrange
        Maquina maquina = criarMaquina("Trator Vermelho", new BigDecimal("500"));

        // Act + Assert
        assertFalse(maquina.equals(criarMaquina("Trator Vermelho", new BigDecimal("600"))));
        assertFalse(maquina.equals(null));
        assertFalse(maquina.equals("Trator Vermelho"));
        assertTrue(maquina.equals(maquina));
    }

    @Test
    void hashCodeDeveSerIgualParaObjetosIguaisEPositivo() {
        // Arrange
        Maquina a = criarMaquina("Trator Vermelho", new BigDecimal("500"));
        Maquina b = criarMaquina("Trator Vermelho", new BigDecimal("500"));

        // Act + Assert
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.hashCode() >= 0);
    }

    @Test
    void compareToDeveOrdenarPorValorDiarioEDepoisPorTitulo() {
        // Arrange
        Maquina barata = criarMaquina("Trator Azul", new BigDecimal("300"));
        Maquina cara = criarMaquina("Trator Azul", new BigDecimal("900"));
        Maquina mesmoValorOutroTitulo = criarMaquina("Trator Vermelho", new BigDecimal("300"));

        // Act + Assert
        assertTrue(barata.compareTo(cara) < 0);
        assertTrue(cara.compareTo(barata) > 0);
        assertTrue(barata.compareTo(mesmoValorOutroTitulo) < 0);
        assertEquals(0, barata.compareTo(criarMaquina("Trator Azul", new BigDecimal("300"))));
    }

    @Test
    void toStringDeveConterTituloTipoMunicipioEValor() {
        // Arrange
        Maquina maquina = criarMaquina("Trator Vermelho", new BigDecimal("500"));

        // Act
        String texto = maquina.toString();

        // Assert
        assertTrue(texto.contains("Trator Vermelho"));
        assertTrue(texto.contains("trator"));
        assertTrue(texto.contains("Paulinia"));
        assertTrue(texto.contains("500"));
    }

    @Test
    void construtorDeCopiaDeveLancarExcecaoQuandoModeloForNulo() {
        // Act + Assert
        assertThrows(Exception.class, () -> new Maquina(null));
    }

    @Test
    void construtorDeCopiaDeveCopiarCamposSemCompartilharLocalizacao() throws Exception {
        // Arrange
        Maquina original = criarMaquina("Trator Vermelho", new BigDecimal("500"));

        // Act
        Maquina copia = new Maquina(original);

        // Assert
        assertEquals(original, copia);
        assertNotSame(original.getLocalizacao(), copia.getLocalizacao());
    }

    @Test
    void cloneDeveGerarCopiaIndependenteEIgual() {
        // Arrange
        Maquina original = criarMaquina("Trator Vermelho", new BigDecimal("500"));

        // Act
        Maquina clone = (Maquina) original.clone();
        clone.setTituloAnuncio("Outro titulo");

        // Assert
        assertNotSame(original, clone);
        assertEquals("Trator Vermelho", original.getTituloAnuncio());
        assertEquals("Outro titulo", clone.getTituloAnuncio());
    }
}
