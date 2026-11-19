/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Endpoint REST do cadastro de maquinas (POST /api/maquinas)
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

import com.alugamaquina.backend.dto.MaquinaCadastroRequest;
import com.alugamaquina.backend.dto.MaquinaCadastroResponse;
import com.alugamaquina.backend.service.MaquinaCadastroService;

// Controller do cadastro de maquinas (a busca por raio fica no MaquinaBuscaController)
@RestController
@RequestMapping("/api/maquinas")
public class MaquinaCadastroController {

    private final MaquinaCadastroService maquinaCadastroService;

    public MaquinaCadastroController(MaquinaCadastroService maquinaCadastroService) {
        this.maquinaCadastroService = maquinaCadastroService;
    }

    // POST /api/maquinas: grava o equipamento e responde 201
    @PostMapping
    public ResponseEntity<MaquinaCadastroResponse> cadastrar(@RequestBody MaquinaCadastroRequest request) {
        MaquinaCadastroResponse response = maquinaCadastroService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Dado invalido vira 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarDadoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "sucesso", false,
                "erro", ex.getMessage()
        ));
    }
}
