/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes unitarios JUnit 5 dos metodos canonicos da entidade Usuario (Issue #46)
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    // Cria um usuario de exemplo para os testes
    private Usuario criarUsuario(String nome, String documento) {
        return new Usuario(nome, documento, "19999999999", nome.toLowerCase() + "@teste.com",
                "Paulinia", "locador", "hash");
    }

    @Test
    void reputacaoInicialDeveSerCincoSemAvaliacoes() {
        Usuario usuario = criarUsuario("Ana", "12345678901");

        assertEquals(5.0, usuario.getReputacaoMedia());
        assertEquals(0, usuario.getTotalAvaliacoes());
    }

    @Test
    void equalsDeveSerVerdadeiroParaMesmosDados() {
        assertEquals(criarUsuario("Ana", "12345678901"), criarUsuario("Ana", "12345678901"));
    }

    @Test
    void equalsDeveSerFalsoParaDocumentoDiferenteNuloOuOutraClasse() {
        Usuario usuario = criarUsuario("Ana", "12345678901");

        assertFalse(usuario.equals(criarUsuario("Ana", "99999999999")));
        assertFalse(usuario.equals(null));
        assertFalse(usuario.equals("Ana"));
        assertTrue(usuario.equals(usuario));
    }

    @Test
    void hashCodeDeveSerIgualParaObjetosIguaisEPositivo() {
        Usuario a = criarUsuario("Ana", "12345678901");
        Usuario b = criarUsuario("Ana", "12345678901");

        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.hashCode() >= 0);
    }

    @Test
    void compareToDeveOrdenarPorNomeEDepoisPorDocumento() {
        Usuario ana = criarUsuario("Ana", "111");
        Usuario bruno = criarUsuario("Bruno", "111");
        Usuario anaOutroDoc = criarUsuario("Ana", "222");

        assertTrue(ana.compareTo(bruno) < 0);
        assertTrue(bruno.compareTo(ana) > 0);
        assertTrue(ana.compareTo(anaOutroDoc) < 0);
        assertEquals(0, ana.compareTo(criarUsuario("Ana", "111")));
    }

    @Test
    void toStringNaoDeveExporASenhaHash() {
        String texto = criarUsuario("Ana", "12345678901").toString();

        assertTrue(texto.contains("Ana"));
        assertFalse(texto.contains("hash"));
    }

    @Test
    void construtorDeCopiaDeveCopiarTodosOsCampos() throws Exception {
        Usuario original = criarUsuario("Ana", "12345678901");
        original.setReputacaoMedia(4.2);
        original.setTotalAvaliacoes(3);

        Usuario copia = new Usuario(original);

        assertEquals(original, copia);
        assertNotSame(original, copia);
    }

    @Test
    void construtorDeCopiaComModeloNuloDeveLancarExcecao() {
        Exception erro = assertThrows(Exception.class, () -> new Usuario((Usuario) null));

        assertEquals("Modelo ausente", erro.getMessage());
    }

    @Test
    void cloneDeveGerarObjetoIgualEIndependente() {
        Usuario original = criarUsuario("Ana", "12345678901");
        Usuario clone = (Usuario) original.clone();

        assertEquals(original, clone);
        assertNotSame(original, clone);

        clone.setNome("Outra");
        assertEquals("Ana", original.getNome());
    }
}
