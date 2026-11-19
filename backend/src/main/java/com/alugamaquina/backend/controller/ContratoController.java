/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Endpoints REST do contrato digital (/api/contratos): gerar, consultar, assinar e baixar o PDF
*/

package com.alugamaquina.backend.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.AssinarContratoRequest;
import com.alugamaquina.backend.dto.ContratoResponse;
import com.alugamaquina.backend.dto.GerarContratoRequest;
import com.alugamaquina.backend.exception.AssinaturaNaoPermitidaException;
import com.alugamaquina.backend.exception.ContratoConflitoException;
import com.alugamaquina.backend.exception.ContratoNaoEncontradoException;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.service.ContratoService;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    // POST /api/contratos/gerar: cria o contrato da locacao aceita e responde 201
    @PostMapping("/gerar")
    public ResponseEntity<ContratoResponse> gerar(@RequestBody GerarContratoRequest request) {
        String locacaoId = request == null ? null : request.getLocacaoId();
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoService.gerar(locacaoId));
    }

    // GET /api/contratos/{id}: dados do contrato
    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponse> buscar(@PathVariable String id) {
        return ResponseEntity.ok(contratoService.buscar(id));
    }

    // GET /api/contratos/locacao/{locacaoId}: contrato de uma locacao
    @GetMapping("/locacao/{locacaoId}")
    public ResponseEntity<ContratoResponse> buscarPorLocacao(@PathVariable String locacaoId) {
        return ResponseEntity.ok(contratoService.buscarPorLocacao(locacaoId));
    }

    // GET /api/contratos/{id}/pdf: baixa o PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable String id) {
        String numero = contratoService.buscar(id).getNumero();
        byte[] pdf = contratoService.gerarPdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contrato-" + numero + ".pdf\"")
                .body(pdf);
    }

    // PUT /api/contratos/{id}/assinar: registra a assinatura de quem esta logado
    @PutMapping("/{id}/assinar")
    public ResponseEntity<ContratoResponse> assinar(@PathVariable String id,
                                                    @RequestBody AssinarContratoRequest request) {
        String usuarioId = request == null ? null : request.getUsuarioId();
        return ResponseEntity.ok(contratoService.assinar(id, usuarioId));
    }

    // Dado invalido vira 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Contrato ou locacao inexistente vira 404
    @ExceptionHandler({ContratoNaoEncontradoException.class, LocacaoNaoEncontradaException.class})
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RuntimeException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Acao que nao pode ser feita agora vira 409
    @ExceptionHandler(ContratoConflitoException.class)
    public ResponseEntity<Map<String, Object>> tratarConflito(ContratoConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Quem nao e parte do contrato vira 403
    @ExceptionHandler(AssinaturaNaoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> tratarAssinaturaNaoPermitida(AssinaturaNaoPermitidaException ex) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String erro) {
        return ResponseEntity.status(status).body(Map.of(
                "sucesso", false,
                "erro", erro
        ));
    }
}
