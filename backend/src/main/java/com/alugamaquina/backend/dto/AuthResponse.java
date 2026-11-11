/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: DTO de resposta para operacoes de autenticacao com token e dados basicos
*/

package com.alugamaquina.backend.dto;

public class AuthResponse {

    private boolean sucesso;
    private String mensagem;
    private String id;
    private String nome;
    private String email;
    private String documento;
    private String perfil;
    private String municipio;

    public AuthResponse() {
    }

    public AuthResponse(boolean sucesso, String mensagem, String id, String nome, String email, String documento, String perfil, String municipio) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.documento = documento;
        this.perfil = perfil;
        this.municipio = municipio;
    }

    public static AuthResponse sucesso(String mensagem, String id, String nome, String email, String documento, String perfil, String municipio) {
        return new AuthResponse(true, mensagem, id, nome, email, documento, perfil, municipio);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }
}

