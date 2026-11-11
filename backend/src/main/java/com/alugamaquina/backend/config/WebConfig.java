/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Configuracao global de CORS para integracao com o front-end
*/

package com.alugamaquina.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracao de CORS para permitir a comunicacao das telas HTML/JS
 * com os endpoints da API REST do backend Spring Boot.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #21)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

