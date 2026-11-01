package com.alugamaquina.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alugamaquina.backend.dto.MaquinaBuscaResponse;
import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;
import com.alugamaquina.backend.service.MaquinaBuscaService;

@RestController
@RequestMapping("/api/maquinas")
public class MaquinaBuscaController {

    private final MaquinaBuscaService maquinaBuscaService;

    public MaquinaBuscaController(MaquinaBuscaService maquinaBuscaService) {
        this.maquinaBuscaService = maquinaBuscaService;
    }

    @GetMapping("/busca")
    public List<MaquinaBuscaResponse> buscar(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double raioKm) {
        return maquinaBuscaService.buscarPorRaio(lat, lng, raioKm);
    }

    @ExceptionHandler(ParametroBuscaInvalidoException.class)
    public ResponseEntity<Map<String, String>> tratarParametroInvalido(ParametroBuscaInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", ex.getMessage()));
    }
}
