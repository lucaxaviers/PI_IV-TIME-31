/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Dados enviados pelo formulario de cadastro de maquinas, com validacoes estaticas e metodos canonicos
*/

package com.alugamaquina.backend.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.alugamaquina.backend.model.MunicipioAtendido;
import com.alugamaquina.backend.model.TipoEquipamento;

public class MaquinaCadastroRequest implements Comparable<MaquinaCadastroRequest>, Cloneable {

    // Limites do titulo e do valor da diaria
    public static final int TITULO_MINIMO = 5;
    public static final int TITULO_MAXIMO = 100;
    public static final BigDecimal VALOR_DIARIA_MAXIMO = new BigDecimal("50000.00");

    private String tituloAnuncio;
    private String tipoEquipamento;
    private String municipio;
    private BigDecimal valorDiaria;

    // Construtor vazio: o Jackson usa para ler o JSON; quem valida e o servico
    public MaquinaCadastroRequest() {
    }

    // Construtor principal: valida todos os parametros
    public MaquinaCadastroRequest(String tituloAnuncio, String tipoEquipamento,
                                  String municipio, BigDecimal valorDiaria) throws Exception {
        if (!MaquinaCadastroRequest.isTituloValido(tituloAnuncio))
            throw new Exception("Titulo do anuncio invalido");
        if (!MaquinaCadastroRequest.isTipoEquipamentoValido(tipoEquipamento))
            throw new Exception("Tipo de equipamento invalido");
        if (!MaquinaCadastroRequest.isMunicipioValido(municipio))
            throw new Exception("Municipio invalido");
        if (!MaquinaCadastroRequest.isValorDiariaValido(valorDiaria))
            throw new Exception("Valor da diaria invalido");

        this.tituloAnuncio = tituloAnuncio.trim();
        this.tipoEquipamento = tipoEquipamento.trim().toLowerCase();
        this.municipio = municipio.trim().toLowerCase();
        this.valorDiaria = valorDiaria.setScale(2, RoundingMode.HALF_UP);
    }

    // ---- Validacoes estaticas (podem ser usadas antes de criar o objeto) ----

    // Titulo de 5 a 100 caracteres, sem contar os espacos das pontas
    public static boolean isTituloValido(String tituloAnuncio) {
        if (tituloAnuncio == null)
            return false;

        int tamanho = tituloAnuncio.trim().length();
        return tamanho >= TITULO_MINIMO && tamanho <= TITULO_MAXIMO;
    }

    public static boolean isTipoEquipamentoValido(String tipoEquipamento) {
        return TipoEquipamento.isCodigoValido(tipoEquipamento);
    }

    public static boolean isMunicipioValido(String municipio) {
        return MunicipioAtendido.isCodigoValido(municipio);
    }

    // Valor maior que zero e de no maximo R$ 50.000,00
    public static boolean isValorDiariaValido(BigDecimal valorDiaria) {
        return valorDiaria != null
                && valorDiaria.signum() > 0
                && valorDiaria.compareTo(VALOR_DIARIA_MAXIMO) <= 0;
    }

    // Verdadeiro se os quatro campos estao validos
    public boolean isValido() {
        return MaquinaCadastroRequest.isTituloValido(this.tituloAnuncio)
                && MaquinaCadastroRequest.isTipoEquipamentoValido(this.tipoEquipamento)
                && MaquinaCadastroRequest.isMunicipioValido(this.municipio)
                && MaquinaCadastroRequest.isValorDiariaValido(this.valorDiaria);
    }

    // ---- Getters e setters (os setters nao validam: o JSON chega por eles e o servico valida) ----

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

    // ---- Metodos canonicos ----

    // Construtor de copia
    public MaquinaCadastroRequest(MaquinaCadastroRequest modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.tituloAnuncio = modelo.tituloAnuncio;
        this.tipoEquipamento = modelo.tipoEquipamento;
        this.municipio = modelo.municipio;
        this.valorDiaria = modelo.valorDiaria;     // BigDecimal e imutavel
    }

    @Override
    public Object clone() {
        MaquinaCadastroRequest retorno = null;

        try {
            retorno = new MaquinaCadastroRequest(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return this.tituloAnuncio + " - " + this.tipoEquipamento + " - " + this.municipio
                + " - R$ " + this.valorDiaria;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        MaquinaCadastroRequest req = (MaquinaCadastroRequest) obj;

        if (!Objects.equals(this.tituloAnuncio, req.tituloAnuncio)) return false;
        if (!Objects.equals(this.tipoEquipamento, req.tipoEquipamento)) return false;
        if (!Objects.equals(this.municipio, req.municipio)) return false;
        if (!Objects.equals(this.valorDiaria, req.valorDiaria)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.tituloAnuncio == null ? 0 : this.tituloAnuncio.hashCode());
        retorno = retorno * 2 + (this.tipoEquipamento == null ? 0 : this.tipoEquipamento.hashCode());
        retorno = retorno * 2 + (this.municipio == null ? 0 : this.municipio.hashCode());
        retorno = retorno * 2 + (this.valorDiaria == null ? 0 : this.valorDiaria.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    // Ordem natural: pelo titulo do anuncio e, se empatar, pelo valor da diaria
    @Override
    public int compareTo(MaquinaCadastroRequest req) {
        int porTitulo = MaquinaCadastroRequest.comparar(this.tituloAnuncio, req.tituloAnuncio);
        if (porTitulo != 0) return porTitulo;

        if (this.valorDiaria == null && req.valorDiaria == null) return 0;
        if (this.valorDiaria == null) return -666;
        if (req.valorDiaria == null) return 666;

        int porValor = this.valorDiaria.compareTo(req.valorDiaria);
        if (porValor < 0) return -666;
        if (porValor > 0) return 666;
        return 0;
    }

    // Compara dois textos aceitando nulo (nulo vem primeiro)
    private static int comparar(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -666;
        if (b == null) return 666;

        int resultado = a.compareTo(b);
        if (resultado < 0) return -666;
        if (resultado > 0) return 666;
        return 0;
    }
}
