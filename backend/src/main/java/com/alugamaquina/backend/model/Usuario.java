/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Modelo de entidade Usuario para autenticacao e reputacao no sistema
*/

package com.alugamaquina.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidade que representa os usuarios (locadores e locatarios) da plataforma AlugaMaquina.
 */
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String documento; // CPF ou CNPJ

    private String telefone;

    @Indexed(unique = true)
    private String email;

    private String municipio;

    private String perfil; // locatario, locador, ambos

    private String senhaHash;

    private Double reputacaoMedia = 5.0; // Inicia com reputacao maxima neutra

    private Integer totalAvaliacoes = 0;

    public Usuario() {
    }

    public Usuario(String nome, String documento, String telefone, String email, String municipio, String perfil, String senhaHash) {
        this.nome = nome;
        this.documento = documento;
        this.telefone = telefone;
        this.email = email;
        this.municipio = municipio;
        this.perfil = perfil;
        this.senhaHash = senhaHash;
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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Double getReputacaoMedia() {
        return reputacaoMedia;
    }

    public void setReputacaoMedia(Double reputacaoMedia) {
        this.reputacaoMedia = reputacaoMedia;
    }

    public Integer getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(Integer totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }
}

