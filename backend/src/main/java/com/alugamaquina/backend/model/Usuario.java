/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Modelo de entidade Usuario para autenticacao e reputacao no sistema
*/

package com.alugamaquina.backend.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidade que representa os usuarios (locadores e locatarios) da plataforma AlugaMaquina.
 */
@Document(collection = "usuarios")
public class Usuario implements Comparable<Usuario>, Cloneable {

    @Id
    private String id;

    // Dados pessoais e de contato
    private String nome;

    @Indexed(unique = true)
    private String documento; // CPF ou CNPJ

    private String telefone;

    @Indexed(unique = true)
    private String email;

    private String municipio;

    // Perfil de acesso: locatario, locador ou ambos
    private String perfil; // locatario, locador, ambos

    // Senha guardada so como hash (nunca em texto puro)
    private String senhaHash;

    // Reputacao do locador, atualizada a cada avaliacao
    private Double reputacaoMedia = 5.0; // Inicia com reputacao maxima neutra

    private Integer totalAvaliacoes = 0;

    // Construtor vazio: o Spring Data usa para ler do banco
    public Usuario() {
    }

    // Construtor com os dados do cadastro
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

    // ---- Metodos canonicos (padrao Prof. Francisco) ----

    public Usuario(Usuario modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.nome = modelo.nome;
        this.documento = modelo.documento;
        this.telefone = modelo.telefone;
        this.email = modelo.email;
        this.municipio = modelo.municipio;
        this.perfil = modelo.perfil;
        this.senhaHash = modelo.senhaHash;
        this.reputacaoMedia = modelo.reputacaoMedia;
        this.totalAvaliacoes = modelo.totalAvaliacoes;
    }

    @Override
    public Object clone() {
        Usuario retorno = null;

        try {
            retorno = new Usuario(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        // a senhaHash nunca aparece no texto, por seguranca
        return this.nome + " (" + this.perfil + ") - " + this.email
                + " - reputacao " + this.reputacaoMedia
                + " em " + this.totalAvaliacoes + " avaliacoes";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        Usuario usu = (Usuario) obj;

        if (!Objects.equals(this.id, usu.id)) return false;
        if (!Objects.equals(this.nome, usu.nome)) return false;
        if (!Objects.equals(this.documento, usu.documento)) return false;
        if (!Objects.equals(this.telefone, usu.telefone)) return false;
        if (!Objects.equals(this.email, usu.email)) return false;
        if (!Objects.equals(this.municipio, usu.municipio)) return false;
        if (!Objects.equals(this.perfil, usu.perfil)) return false;
        if (!Objects.equals(this.senhaHash, usu.senhaHash)) return false;
        if (!Objects.equals(this.reputacaoMedia, usu.reputacaoMedia)) return false;
        if (!Objects.equals(this.totalAvaliacoes, usu.totalAvaliacoes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.nome == null ? 0 : this.nome.hashCode());
        retorno = retorno * 2 + (this.documento == null ? 0 : this.documento.hashCode());
        retorno = retorno * 2 + (this.telefone == null ? 0 : this.telefone.hashCode());
        retorno = retorno * 2 + (this.email == null ? 0 : this.email.hashCode());
        retorno = retorno * 2 + (this.municipio == null ? 0 : this.municipio.hashCode());
        retorno = retorno * 2 + (this.perfil == null ? 0 : this.perfil.hashCode());
        retorno = retorno * 2 + (this.senhaHash == null ? 0 : this.senhaHash.hashCode());
        retorno = retorno * 2 + (this.reputacaoMedia == null ? 0 : this.reputacaoMedia.hashCode());
        retorno = retorno * 2 + (this.totalAvaliacoes == null ? 0 : this.totalAvaliacoes.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    @Override
    public int compareTo(Usuario usu) {
        int porNome = comparar(this.nome, usu.nome);
        if (porNome != 0) return porNome;

        return comparar(this.documento, usu.documento);
    }

    private static int comparar(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -666;
        if (b == null) return 666;

        int c = a.compareToIgnoreCase(b);
        if (c < 0) return -666;
        if (c > 0) return 666;
        return 0;
    }
}