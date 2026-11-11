/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Servico de regras de negocio para autenticacao, cadastro e hashing de senhas
*/

package com.alugamaquina.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.AuthResponse;
import com.alugamaquina.backend.dto.CadastroRequest;
import com.alugamaquina.backend.dto.LoginRequest;
import com.alugamaquina.backend.exception.AutenticacaoException;
import com.alugamaquina.backend.exception.UsuarioJaCadastradoException;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Realiza o cadastro de um novo produtor (locador, locatario ou ambos).
     */
    public AuthResponse cadastrar(CadastroRequest request) {
        validarCadastro(request);

        String emailNormalizado = request.getEmail().trim().toLowerCase();
        String docNormalizado = limparDocumento(request.getDocumento());

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new UsuarioJaCadastradoException("E-mail ja cadastrado na plataforma.");
        }

        if (usuarioRepository.existsByDocumento(docNormalizado)) {
            throw new UsuarioJaCadastradoException("CPF/CNPJ ja cadastrado na plataforma.");
        }

        String senhaHash = gerarHashSenha(request.getSenha().trim());

        Usuario novoUsuario = new Usuario(
                request.getNome().trim(),
                docNormalizado,
                request.getTelefone() != null ? request.getTelefone().trim() : "",
                emailNormalizado,
                request.getMunicipio() != null ? request.getMunicipio().trim() : "",
                request.getPerfil() != null ? request.getPerfil().trim().toLowerCase() : "ambos",
                senhaHash
        );

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        return AuthResponse.sucesso(
                "Cadastro realizado com sucesso!",
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getDocumento(),
                usuarioSalvo.getPerfil(),
                usuarioSalvo.getMunicipio()
        );
    }

    /**
     * Autentica o usuario por e-mail ou documento (CPF/CNPJ) e confere a senha.
     */
    public AuthResponse autenticar(LoginRequest request) {
        if (request.getIdentificador() == null || request.getIdentificador().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe seu e-mail ou CPF/CNPJ para entrar.");
        }
        if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a sua senha.");
        }

        String identificador = request.getIdentificador().trim();
        String identificadorDoc = limparDocumento(identificador);
        String identificadorEmail = identificador.toLowerCase();

        // Tenta buscar por e-mail ou documento
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(identificadorEmail);
        if (optUsuario.isEmpty()) {
            optUsuario = usuarioRepository.findByDocumento(identificadorDoc);
        }

        if (optUsuario.isEmpty()) {
            throw new AutenticacaoException("E-mail/CPF ou senha incorretos.");
        }

        Usuario usuario = optUsuario.get();
        String hashDigitado = gerarHashSenha(request.getSenha().trim());

        if (!usuario.getSenhaHash().equals(hashDigitado)) {
            throw new AutenticacaoException("E-mail/CPF ou senha incorretos.");
        }

        return AuthResponse.sucesso(
                "Login realizado com sucesso!",
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDocumento(),
                usuario.getPerfil(),
                usuario.getMunicipio()
        );
    }

    private void validarCadastro(CadastroRequest request) {
        if (request.getNome() == null || request.getNome().trim().length() < 3) {
            throw new IllegalArgumentException("Nome obrigatorio (minimo 3 caracteres).");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("E-mail informado e invalido.");
        }
        if (request.getDocumento() == null || limparDocumento(request.getDocumento()).length() < 11) {
            throw new IllegalArgumentException("CPF ou CNPJ obrigatorio e valido.");
        }
        if (request.getSenha() == null || request.getSenha().trim().length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no minimo 6 caracteres.");
        }
    }

    /**
     * Remove pontuacoes e caracteres especiais de CPF/CNPJ.
     */
    private String limparDocumento(String documento) {
        if (documento == null) return "";
        return documento.replaceAll("[^0-9]", "");
    }

    /**
     * Gera o hash SHA-256 da senha de forma segura e nativa do Java.
     */
    public String gerarHashSenha(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao processar hash de criptografia.", e);
        }
    }
}

