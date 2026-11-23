/*
  Autor: Time 31 - PI IV (Lucas Rodrigues Xavier | 25000508, Leonardo Gambaroni Alves | 25003494, Mateus Oliveira Rafael | 25001369, Kauan Aurélio Lasmar Dias | 25001590, Tiago Medeiros | 25000845)
  Descricao: Classe principal de inicializacao da API REST Spring Boot do AlugaMaquina (Issue #21)
*/

package com.alugamaquina.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
