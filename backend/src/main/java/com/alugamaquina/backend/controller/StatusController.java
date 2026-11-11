package com.alugamaquina.backend.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint basico de teste e verificacao de saude do servidor Java Spring Boot.
 * Utilizado para confirmar que a API REST basica esta ativa e respondendo requisicoes.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #21)
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> verificarStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("sistema", "AlugaMaquina API");
        status.put("versao", "0.1.0");
        status.put("servidor", "Spring Boot (Java)");
        status.put("status", "ONLINE");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(status);
    }
}

