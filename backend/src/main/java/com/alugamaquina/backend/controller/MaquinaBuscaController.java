/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Endpoints REST de busca geoespacial por raio e detalhe de maquinas (Issue #36)
*/

package com.alugamaquina.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.MaquinaBuscaResponse;
import com.alugamaquina.backend.dto.MaquinaDetalheResponse;
import com.alugamaquina.backend.exception.MaquinaNaoEncontradaException;
import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;
import com.alugamaquina.backend.service.MaquinaBuscaService;

@RestController
@RequestMapping("/api/maquinas")
public class MaquinaBuscaController {

    private final MaquinaBuscaService maquinaBuscaService;

    public MaquinaBuscaController(MaquinaBuscaService maquinaBuscaService) {
        this.maquinaBuscaService = maquinaBuscaService;
    }

    // GET /api/maquinas/busca: consulta equipamentos dentro do raio em km (padrao 50 km)
    @GetMapping("/busca")
    public List<MaquinaBuscaResponse> buscar(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double raioKm) {
        return maquinaBuscaService.buscarPorRaio(lat, lng, raioKm);
    }

    // GET /api/maquinas/{id}: devolve os dados completos de uma maquina para a tela de detalhe
    @GetMapping("/{id}")
    public MaquinaDetalheResponse buscarPorId(@PathVariable String id) {
        return maquinaBuscaService.buscarPorId(id);
    }

    // Parametros fora da faixa (latitude, longitude ou raio) retornam 400 Bad Request
    @ExceptionHandler(ParametroBuscaInvalidoException.class)
    public ResponseEntity<Map<String, String>> tratarParametroInvalido(ParametroBuscaInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", ex.getMessage()));
    }

    // Id inexistente ou em branco retorna 404 Not Found
    @ExceptionHandler(MaquinaNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> tratarNaoEncontrada(MaquinaNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", ex.getMessage()));
    }
}
