/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: DTO com as credenciais de email e senha para login de usuario
*/

package com.alugamaquina.backend.dto;

// Dados do login: e-mail ou CPF/CNPJ e senha
public class LoginRequest {

    private String identificador; // E-mail ou CPF/CNPJ
    private String senha;

    // Construtor vazio: o Spring usa para ler o JSON
    public LoginRequest() {
    }

    // Construtor com e-mail/CPF e senha
    public LoginRequest(String identificador, String senha) {
        this.identificador = identificador;
        this.senha = senha;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

