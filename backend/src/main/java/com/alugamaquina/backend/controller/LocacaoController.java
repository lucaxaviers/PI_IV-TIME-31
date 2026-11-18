/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Endpoints REST para solicitar, aceitar e recusar locações (/api/locacoes).
 */

package com.alugamaquina.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.LocacaoResponse;
import com.alugamaquina.backend.dto.RecusaLocacaoRequest;
import com.alugamaquina.backend.dto.SolicitacaoLocacaoRequest;
import com.alugamaquina.backend.exception.ConflitoAgendaException;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.TransicaoStatusInvalidaException;
import com.alugamaquina.backend.service.LocacaoService;

@RestController
@RequestMapping("/api/locacoes")
public class LocacaoController {

    private final LocacaoService locacaoService;

    public LocacaoController(LocacaoService locacaoService) {
        this.locacaoService = locacaoService;
    }

    // POST /api/locacoes/solicitar: cria a locacao PENDENTE e responde 201
    @PostMapping("/solicitar")
    public ResponseEntity<LocacaoResponse> solicitar(@RequestBody SolicitacaoLocacaoRequest request) {
        LocacaoResponse response = locacaoService.solicitar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/locacoes/{id}/aceitar: locador aceita e responde 200
    @PutMapping("/{id}/aceitar")
    public ResponseEntity<LocacaoResponse> aceitar(@PathVariable String id) {
        return ResponseEntity.ok(locacaoService.aceitar(id));
    }

    // PUT /api/locacoes/{id}/recusar: locador recusa (corpo com o motivo e opcional)
    @PutMapping("/{id}/recusar")
    public ResponseEntity<LocacaoResponse> recusar(@PathVariable String id,
                                                   @RequestBody(required = false) RecusaLocacaoRequest request) {
        String motivo = request == null ? null : request.getMotivo();
        return ResponseEntity.ok(locacaoService.recusar(id, motivo));
    }

    // Dado invalido vira 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Locacao inexistente vira 404
    @ExceptionHandler(LocacaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrada(LocacaoNaoEncontradaException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Maquina ja reservada no periodo vira 409
    @ExceptionHandler(ConflitoAgendaException.class)
    public ResponseEntity<Map<String, Object>> tratarConflitoAgenda(ConflitoAgendaException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Status que nao permite a acao vira 409
    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<Map<String, Object>> tratarTransicaoInvalida(TransicaoStatusInvalidaException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String erro) {
        return ResponseEntity.status(status).body(Map.of(
                "sucesso", false,
                "erro", erro
        ));
    }
}
