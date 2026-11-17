/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes JUnit 5 de AuthService com Particionamento de Equivalencia e BVA,
             no padrao AAA (Issue #46)

  Fronteiras testadas:
    nome      2 (invalido) | 3 (valido)
    senha     5 (invalida) | 6 (valida)
    documento 10 digitos (invalido) | 11 digitos (valido), com e sem mascara
    e-mail    sem "@" (invalido) | com "@" (valido)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.alugamaquina.backend.dto.AuthResponse;
import com.alugamaquina.backend.dto.CadastroRequest;
import com.alugamaquina.backend.dto.LoginRequest;
import com.alugamaquina.backend.exception.UsuarioJaCadastradoException;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.UsuarioRepository;

class AuthServiceBvaTest {

    private UsuarioRepository usuarioRepository;
    private AuthService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AuthService(usuarioRepository);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
    }

    private CadastroRequest cadastro(String nome, String documento, String email, String senha) {
        return new CadastroRequest(nome, documento, "19999999999", email, "Paulinia", "locador", senha);
    }

    // ---------- Nome ----------

    @ParameterizedTest
    @ValueSource(strings = {"", "A", "Ab", "  Ab  "})
    void nomeComMenosDeTresCaracteresDeveLancarExcecao(String nome) {
        CadastroRequest request = cadastro(nome, "12345678901", "a@b.com", "123456");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(request));
    }

    @Test
    void nomeComExatamenteTresCaracteresDeveSerAceito() {
        CadastroRequest request = cadastro("Ana", "12345678901", "a@b.com", "123456");

        assertDoesNotThrow(() -> service.cadastrar(request));
    }

    // ---------- Senha ----------

    @ParameterizedTest
    @ValueSource(strings = {"", "1", "12345", "  12345  "})
    void senhaComMenosDeSeisCaracteresDeveLancarExcecao(String senha) {
        CadastroRequest request = cadastro("Ana Souza", "12345678901", "a@b.com", senha);

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "1234567", "senhaBemLonga123"})
    void senhaComSeisOuMaisCaracteresDeveSerAceita(String senha) {
        CadastroRequest request = cadastro("Ana Souza", "12345678901", "a@b.com", senha);

        assertDoesNotThrow(() -> service.cadastrar(request));
    }

    // ---------- Documento ----------

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "1234567890", "123.456.789-0"})
    void documentoComMenosDeOnzeDigitosDeveLancarExcecao(String documento) {
        CadastroRequest request = cadastro("Ana Souza", documento, "a@b.com", "123456");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(request));
    }

    @Test
    void documentoComMascaraDeveSerGravadoSomenteComDigitos() {
        // Arrange
        CadastroRequest request = cadastro("Ana Souza", "123.456.789-01", "a@b.com", "123456");

        // Action
        AuthResponse response = service.cadastrar(request);

        // Assert
        assertEquals("12345678901", response.getDocumento());
    }

    @Test
    void documentoCnpjDeCatorzeDigitosDeveSerAceito() {
        CadastroRequest request = cadastro("Fazenda Sol", "12.345.678/0001-90", "f@b.com", "123456");

        assertDoesNotThrow(() -> service.cadastrar(request));
    }

    // ---------- E-mail ----------

    @ParameterizedTest
    @ValueSource(strings = {"", "semarroba.com", "usuario.com.br"})
    void emailSemArrobaDeveLancarExcecao(String email) {
        CadastroRequest request = cadastro("Ana Souza", "12345678901", email, "123456");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(request));
    }

    @Test
    void emailDeveSerGravadoEmMinusculasESemEspacos() {
        // Arrange
        CadastroRequest request = cadastro("Ana Souza", "12345678901", "  ANA@Teste.COM ", "123456");

        // Action
        AuthResponse response = service.cadastrar(request);

        // Assert
        assertEquals("ana@teste.com", response.getEmail());
    }

    // ---------- Duplicidade (particao: novo | ja cadastrado) ----------

    @Test
    void documentoJaCadastradoDeveLancarExcecao() {
        // Arrange
        when(usuarioRepository.existsByDocumento("12345678901")).thenReturn(true);
        CadastroRequest request = cadastro("Ana Souza", "123.456.789-01", "novo@b.com", "123456");

        // Action + Assert
        assertThrows(UsuarioJaCadastradoException.class, () -> service.cadastrar(request));
    }

    // ---------- Hash da senha ----------

    @Test
    void senhaNuncaDeveSerGravadaEmTextoPuro() {
        // Arrange
        java.util.concurrent.atomic.AtomicReference<Usuario> gravado = new java.util.concurrent.atomic.AtomicReference<>();
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            gravado.set(i.getArgument(0));
            return i.getArgument(0);
        });

        // Action
        service.cadastrar(cadastro("Ana Souza", "12345678901", "a@b.com", "minhaSenha1"));

        // Assert: SHA-256 em hexadecimal tem 64 caracteres e e diferente da senha original
        assertNotEquals("minhaSenha1", gravado.get().getSenhaHash());
        assertEquals(64, gravado.get().getSenhaHash().length());
    }

    @Test
    void mesmaSenhaDeveGerarSempreOMesmoHash() {
        // Arrange
        java.util.List<String> hashes = new java.util.ArrayList<>();
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            hashes.add(u.getSenhaHash());
            return u;
        });

        // Action
        service.cadastrar(cadastro("Ana Souza", "12345678901", "a@b.com", "minhaSenha1"));
        service.cadastrar(cadastro("Bia Souza", "10987654321", "b@b.com", "minhaSenha1"));

        // Assert
        assertEquals(hashes.get(0), hashes.get(1));
    }

    // ---------- Login: particao de entradas vazias ----------

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void loginComIdentificadorVazioDeveLancarExcecao(String identificador) {
        LoginRequest request = new LoginRequest();
        request.setIdentificador(identificador);
        request.setSenha("123456");

        assertThrows(IllegalArgumentException.class, () -> service.autenticar(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void loginComSenhaVaziaDeveLancarExcecao(String senha) {
        LoginRequest request = new LoginRequest();
        request.setIdentificador("a@b.com");
        request.setSenha(senha);

        assertThrows(IllegalArgumentException.class, () -> service.autenticar(request));
    }
}
