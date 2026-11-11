/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Endpoints REST para registro e consulta de avaliacoes (/api/avaliacoes)
*/

package com.alugamaquina.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.AvaliacaoRequest;
import com.alugamaquina.backend.dto.AvaliacaoResponse;
import com.alugamaquina.backend.model.Avaliacao;
import com.alugamaquina.backend.service.AvaliacaoService;

/**
 * Controller REST para cadastro de avaliacoes pos-locacao e consulta de reputacao.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponse> registrarAvaliacao(@RequestBody AvaliacaoRequest request) {
        AvaliacaoResponse response = avaliacaoService.registrarAvaliacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/locador/{locadorId}")
    public ResponseEntity<List<Avaliacao>> listarPorLocador(@PathVariable String locadorId) {
        List<Avaliacao> avaliacoes = avaliacaoService.listarPorLocador(locadorId);
        return ResponseEntity.ok(avaliacoes);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "sucesso", false,
                "erro", ex.getMessage()
        ));
    }
}
