/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Endpoints REST da vistoria tecnica (/api/vistorias): abrir, consultar e concluir o checklist
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.ConcluirVistoriaRequest;
import com.alugamaquina.backend.dto.CriarVistoriaRequest;
import com.alugamaquina.backend.dto.VistoriaResponse;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.VistoriaConflitoException;
import com.alugamaquina.backend.exception.VistoriaNaoEncontradaException;
import com.alugamaquina.backend.service.VistoriaChecklistService;

@RestController
@RequestMapping("/api/vistorias")
public class VistoriaChecklistController {

    private final VistoriaChecklistService vistoriaService;

    public VistoriaChecklistController(VistoriaChecklistService vistoriaService) {
        this.vistoriaService = vistoriaService;
    }

    // POST /api/vistorias: abre a vistoria com os itens padrao e responde 201
    @PostMapping
    public ResponseEntity<VistoriaResponse> abrir(@RequestBody CriarVistoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vistoriaService.abrir(request));
    }

    // GET /api/vistorias/{id}: dados da vistoria
    @GetMapping("/{id}")
    public ResponseEntity<VistoriaResponse> buscar(@PathVariable String id) {
        return ResponseEntity.ok(vistoriaService.buscar(id));
    }

    // GET /api/vistorias/locacao/{locacaoId}: vistorias da locacao
    @GetMapping("/locacao/{locacaoId}")
    public ResponseEntity<List<VistoriaResponse>> listarPorLocacao(@PathVariable String locacaoId) {
        return ResponseEntity.ok(vistoriaService.listarPorLocacao(locacaoId));
    }

    // PUT /api/vistorias/{id}/concluir: registra as respostas e conclui
    @PutMapping("/{id}/concluir")
    public ResponseEntity<VistoriaResponse> concluir(@PathVariable String id,
                                                     @RequestBody ConcluirVistoriaRequest request) {
        return ResponseEntity.ok(vistoriaService.concluir(id, request));
    }

    // Dado invalido vira 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Vistoria ou locacao inexistente vira 404
    @ExceptionHandler({VistoriaNaoEncontradaException.class, LocacaoNaoEncontradaException.class})
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RuntimeException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Acao que nao pode ser feita agora vira 409
    @ExceptionHandler(VistoriaConflitoException.class)
    public ResponseEntity<Map<String, Object>> tratarConflito(VistoriaConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String erro) {
        return ResponseEntity.status(status).body(Map.of(
                "sucesso", false,
                "erro", erro
        ));
    }
}
