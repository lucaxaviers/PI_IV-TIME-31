/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Modelo de entidade Maquina, com localizacao geoespacial (2dsphere) usada na busca por raio
*/

package com.alugamaquina.backend.model;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidade que representa um equipamento anunciado na plataforma AlugaMaquina.
 *
 * Responsavel: Mateus Oliveira Rafael | 25001369 (Issue #20)
 */
@Document(collection = "maquinas")
public class Maquina implements Comparable<Maquina>, Cloneable {

    @Id
    private String id;

    private String tituloAnuncio;
    private String tipoEquipamento;
    private String municipio;
    private BigDecimal valorDiaria;

    // Indice 2dsphere: permite consulta geoespacial por raio de distancia (geoNear)
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint localizacao;

    // Construtor vazio: o Spring Data usa para ler do banco
    public Maquina() {
    }

    // Construtor com todos os campos
    public Maquina(String tituloAnuncio, String tipoEquipamento, String municipio,
                    BigDecimal valorDiaria, GeoJsonPoint localizacao) {
        this.tituloAnuncio = tituloAnuncio;
        this.tipoEquipamento = tipoEquipamento;
        this.municipio = municipio;
        this.valorDiaria = valorDiaria;
        this.localizacao = localizacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTituloAnuncio() {
        return tituloAnuncio;
    }

    public void setTituloAnuncio(String tituloAnuncio) {
        this.tituloAnuncio = tituloAnuncio;
    }

    public String getTipoEquipamento() {
        return tipoEquipamento;
    }

    public void setTipoEquipamento(String tipoEquipamento) {
        this.tipoEquipamento = tipoEquipamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public GeoJsonPoint getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(GeoJsonPoint localizacao) {
        this.localizacao = localizacao;
    }

    // ---- Metodos canonicos (padrao Prof. Francisco) ----

    public Maquina(Maquina modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.tituloAnuncio = modelo.tituloAnuncio;
        this.tipoEquipamento = modelo.tipoEquipamento;
        this.municipio = modelo.municipio;
        this.valorDiaria = modelo.valorDiaria;
        // GeoJsonPoint e BigDecimal sao imutaveis, mas recriamos a instancia para nao
        // compartilhar referencia com o objeto original
        this.localizacao = modelo.localizacao == null
                ? null
                : new GeoJsonPoint(modelo.localizacao.getX(), modelo.localizacao.getY());
    }

    @Override
    public Object clone() {
        Maquina retorno = null;

        try {
            retorno = new Maquina(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return this.tituloAnuncio + " (" + this.tipoEquipamento + ") - " + this.municipio
                + " - diaria R$ " + this.valorDiaria;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        Maquina maq = (Maquina) obj;

        if (!Objects.equals(this.id, maq.id)) return false;
        if (!Objects.equals(this.tituloAnuncio, maq.tituloAnuncio)) return false;
        if (!Objects.equals(this.tipoEquipamento, maq.tipoEquipamento)) return false;
        if (!Objects.equals(this.municipio, maq.municipio)) return false;
        if (!Objects.equals(this.valorDiaria, maq.valorDiaria)) return false;
        if (!Objects.equals(this.localizacao, maq.localizacao)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.tituloAnuncio == null ? 0 : this.tituloAnuncio.hashCode());
        retorno = retorno * 2 + (this.tipoEquipamento == null ? 0 : this.tipoEquipamento.hashCode());
        retorno = retorno * 2 + (this.municipio == null ? 0 : this.municipio.hashCode());
        retorno = retorno * 2 + (this.valorDiaria == null ? 0 : this.valorDiaria.hashCode());
        retorno = retorno * 2 + (this.localizacao == null ? 0 : this.localizacao.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    @Override
    public int compareTo(Maquina maq) {
        int porValor = comparar(this.valorDiaria, maq.valorDiaria);
        if (porValor != 0) return porValor;

        return comparar(this.tituloAnuncio, maq.tituloAnuncio);
    }

    private static int comparar(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return 0;
        if (a == null) return -666;
        if (b == null) return 666;

        int c = a.compareTo(b);
        if (c < 0) return -666;
        if (c > 0) return 666;
        return 0;
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
