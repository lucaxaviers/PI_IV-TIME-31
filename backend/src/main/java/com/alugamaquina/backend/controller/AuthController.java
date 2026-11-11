/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Endpoints REST para autenticacao (/api/auth/cadastro e /api/auth/login)
*/

package com.alugamaquina.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.AuthResponse;
import com.alugamaquina.backend.dto.CadastroRequest;
import com.alugamaquina.backend.dto.LoginRequest;
import com.alugamaquina.backend.exception.AutenticacaoException;
import com.alugamaquina.backend.exception.UsuarioJaCadastradoException;
import com.alugamaquina.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponse> cadastrar(@RequestBody CadastroRequest request) {
        AuthResponse response = authService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.autenticar(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(AutenticacaoException.class)
    public ResponseEntity<Map<String, Object>> tratarAutenticacao(AutenticacaoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "sucesso", false,
                "erro", ex.getMessage()
        ));
    }

    @ExceptionHandler(UsuarioJaCadastradoException.class)
    public ResponseEntity<Map<String, Object>> tratarUsuarioExistente(UsuarioJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "sucesso", false,
                "erro", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "sucesso", false,
                "erro", ex.getMessage()
        ));
    }
}

