package com.alugamaquina.backend.dto;

public class LoginRequest {

    private String identificador; // E-mail ou CPF/CNPJ
    private String senha;

    public LoginRequest() {
    }

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

