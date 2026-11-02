package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alugamaquina.backend.dto.AuthResponse;
import com.alugamaquina.backend.dto.CadastroRequest;
import com.alugamaquina.backend.dto.LoginRequest;
import com.alugamaquina.backend.exception.AutenticacaoException;
import com.alugamaquina.backend.exception.UsuarioJaCadastradoException;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.UsuarioRepository;

class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        authService = new AuthService(usuarioRepository);
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {
        CadastroRequest request = new CadastroRequest(
                "Antonio Pereira",
                "123.456.789-00",
                "19999990000",
                "antonio@fazenda.com",
                "paulinia",
                "locador",
                "senha123"
        );

        when(usuarioRepository.existsByEmail("antonio@fazenda.com")).thenReturn(false);
        when(usuarioRepository.existsByDocumento("12345678900")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId("id-gerado-123");
            return u;
        });

        AuthResponse response = authService.cadastrar(request);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals("antonio@fazenda.com", response.getEmail());
        assertEquals("12345678900", response.getDocumento());
    }

    @Test
    void deveLancarExcecaoAoCadastrarEmailExistente() {
        CadastroRequest request = new CadastroRequest(
                "Antonio Pereira",
                "123.456.789-00",
                "19999990000",
                "antonio@fazenda.com",
                "paulinia",
                "locador",
                "senha123"
        );

        when(usuarioRepository.existsByEmail("antonio@fazenda.com")).thenReturn(true);

        assertThrows(UsuarioJaCadastradoException.class, () -> authService.cadastrar(request));
    }

    @Test
    void deveLancarExcecaoParaSenhaCurta() {
        CadastroRequest request = new CadastroRequest(
                "Antonio Pereira",
                "123.456.789-00",
                "19999990000",
                "antonio@fazenda.com",
                "paulinia",
                "locador",
                "123"
        );

        assertThrows(IllegalArgumentException.class, () -> authService.cadastrar(request));
    }

    @Test
    void deveAutenticarComSucessoPorEmail() {
        String senha = "senhaSegura123";
        String hash = authService.gerarHashSenha(senha);

        Usuario usuario = new Usuario(
                "Antonio Pereira",
                "12345678900",
                "19999990000",
                "antonio@fazenda.com",
                "paulinia",
                "locador",
                hash
        );
        usuario.setId("user-1");

        when(usuarioRepository.findByEmail("antonio@fazenda.com")).thenReturn(Optional.of(usuario));

        LoginRequest loginRequest = new LoginRequest("antonio@fazenda.com", senha);
        AuthResponse response = authService.autenticar(loginRequest);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals("user-1", response.getId());
    }

    @Test
    void deveLancarExcecaoParaSenhaIncorreta() {
        String hash = authService.gerarHashSenha("senhaCorreta");

        Usuario usuario = new Usuario(
                "Antonio Pereira",
                "12345678900",
                "19999990000",
                "antonio@fazenda.com",
                "paulinia",
                "locador",
                hash
        );

        when(usuarioRepository.findByEmail("antonio@fazenda.com")).thenReturn(Optional.of(usuario));

        LoginRequest loginRequest = new LoginRequest("antonio@fazenda.com", "senhaErrada");

        assertThrows(AutenticacaoException.class, () -> authService.autenticar(loginRequest));
    }

    @Test
    void deveLancarExcecaoParaUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByDocumento("inexistente@email.com")).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest("inexistente@email.com", "qualquerSenha");

        assertThrows(AutenticacaoException.class, () -> authService.autenticar(loginRequest));
    }
}

