/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes OO Intraclasse (Particionamento por Atributos) da entidade Usuario
*/

package com.alugamaquina.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testes OO Intraclasse da classe de dominio Usuario usando Particionamento por Atributos
 * no padrao AAA (Arrange, Action, Assert).
 */
class UsuarioIntraclasseTest {

    // Atributo-chave 1: reputacaoMedia e totalAvaliacoes (usam, modificam e nao modificam)
    @Test
    void atributoReputacaoETotalAvaliacoesDeveReterAlteracoesERefletirEmToStringEqualsHashCode() {
        // Arrange: construtor padrao inicia reputacaoMedia = 5.0 e totalAvaliacoes = 0
        Usuario usuario = new Usuario();

        // Action: leitura inicial -> modificacao -> nova leitura -> consulta textual
        Double reputacaoInicial = usuario.getReputacaoMedia();
        Integer totalInicial = usuario.getTotalAvaliacoes();
        usuario.setReputacaoMedia(4.75);
        usuario.setTotalAvaliacoes(4);
        String resumo = usuario.toString();

        // Assert
        assertEquals(5.0, reputacaoInicial);
        assertEquals(0, totalInicial);
        assertEquals(4.75, usuario.getReputacaoMedia());
        assertEquals(4, usuario.getTotalAvaliacoes());
        assertTrue(resumo.contains("4.75"));
        assertTrue(resumo.contains("4 avaliacoes"));
    }

    // Atributo-chave 2: senhaHash (modificado pelo construtor/setter, nunca exposto no toString)
    @Test
    void atributoSenhaHashDeveParticiparDeEqualsEHashCodeSemVazarNoToString() {
        // Arrange
        Usuario u1 = new Usuario("Seu Antonio", "12345678901", "19999990000",
                "antonio@fazenda.com", "Paulinia", "locador", "hash_inicial_abc");
        Usuario u2 = (Usuario) u1.clone();

        // Action
        u2.setSenhaHash("hash_alterado_xyz");

        // Assert
        assertEquals("hash_inicial_abc", u1.getSenhaHash());
        assertEquals("hash_alterado_xyz", u2.getSenhaHash());
        assertNotEquals(u1, u2);
        assertNotEquals(u1.hashCode(), u2.hashCode());
        assertFalse(u1.toString().contains("hash_inicial_abc"));
        assertFalse(u2.toString().contains("hash_alterado_xyz"));
    }

    // Atributos-chave 3: nome e documento (criterios de ordenacao em compareTo e copia independente)
    @Test
    void atributosNomeEDocumentoDevemControlarOrdenacaoEIndependenciaDeCopia() throws Exception {
        // Arrange
        Usuario primeiro = new Usuario("Ana Maria", "11122233344", "19999991111",
                "ana@sitio.com", "Paulinia", "locatario", "hash1");
        Usuario copia = new Usuario(primeiro);
        Usuario segundoMesmoNome = new Usuario("Ana Maria", "55566677788", "19999992222",
                "ana2@sitio.com", "Campinas", "locador", "hash2");

        // Action
        copia.setNome("Carlos Eduardo");
        int compIgual = primeiro.compareTo(new Usuario(primeiro));
        int compPorDoc = primeiro.compareTo(segundoMesmoNome);
        int compPorNome = primeiro.compareTo(copia);

        // Assert
        assertNotSame(primeiro, copia);
        assertEquals("Ana Maria", primeiro.getNome());
        assertEquals(0, compIgual);
        assertTrue(compPorDoc < 0);
        assertTrue(compPorNome < 0);
    }

    // Particao invalida de atributos / anomalia: setter aceita reputacao negativa sem validacao
    @Test
    void atributosDeReputacaoAceitamValoresForaDaFaixaSemValidacaoDeDominio() {
        // Arrange
        Usuario usuario = new Usuario();

        // Action: comportamento atual permite atribuir valores invalidos na entidade
        usuario.setReputacaoMedia(-1.0);
        usuario.setTotalAvaliacoes(-3);

        // Assert: documenta a anomalia observada na entidade Usuario
        assertEquals(-1.0, usuario.getReputacaoMedia());
        assertEquals(-3, usuario.getTotalAvaliacoes());
        assertThrows(NullPointerException.class, () -> usuario.compareTo(null));
    }
}
