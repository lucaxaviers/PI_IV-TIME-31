/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Entidade Contrato (collection "contratos") com as assinaturas das duas partes e os metodos canonicos
*/

package com.alugamaquina.backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contratos")
public class Contrato implements Comparable<Contrato>, Cloneable {

    // Formato do numero do contrato: AM-ANO-00123
    public static final String PREFIXO_NUMERO = "AM";

    @Id
    private String id;

    // Um contrato por locacao
    @Indexed(unique = true)
    private String locacaoId;

    private String numero;

    private String locadorId;
    private String locatarioId;
    private String maquinaId;

    // Nomes e equipamento ficam guardados no contrato, como em um documento assinado
    private String nomeLocador;
    private String nomeLocatario;
    private String descricaoMaquina;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Valores em reais, sempre com 2 casas decimais
    private BigDecimal valorTotal;
    private BigDecimal valorCaucao;

    private boolean assinadoPeloLocador;
    private boolean assinadoPeloLocatario;
    private LocalDateTime dataAssinaturaLocador;
    private LocalDateTime dataAssinaturaLocatario;

    private LocalDateTime dataEmissao;

    // Construtor vazio: o Spring Data usa para ler do banco
    public Contrato() {
    }

    // Construtor de um novo contrato: nasce sem assinaturas e valida todos os parametros
    public Contrato(String locacaoId, String numero, String locadorId, String locatarioId, String maquinaId,
                    String nomeLocador, String nomeLocatario, String descricaoMaquina,
                    LocalDate dataInicio, LocalDate dataFim,
                    BigDecimal valorTotal, BigDecimal valorCaucao) throws Exception {
        if (!Contrato.isTextoValido(locacaoId))
            throw new Exception("Locacao ausente");
        if (!Contrato.isNumeroValido(numero))
            throw new Exception("Numero do contrato invalido");
        if (!Contrato.isTextoValido(locadorId))
            throw new Exception("Locador ausente");
        if (!Contrato.isTextoValido(locatarioId))
            throw new Exception("Locatario ausente");
        if (!Contrato.isTextoValido(maquinaId))
            throw new Exception("Maquina ausente");
        if (!Contrato.isTextoValido(nomeLocador))
            throw new Exception("Nome do locador ausente");
        if (!Contrato.isTextoValido(nomeLocatario))
            throw new Exception("Nome do locatario ausente");
        if (!Contrato.isTextoValido(descricaoMaquina))
            throw new Exception("Descricao da maquina ausente");
        if (!Contrato.isPeriodoValido(dataInicio, dataFim))
            throw new Exception("Periodo invalido");
        if (!Contrato.isValorTotalValido(valorTotal))
            throw new Exception("Valor total invalido");
        if (!Contrato.isValorCaucaoValido(valorCaucao))
            throw new Exception("Valor da caucao invalido");

        this.locacaoId = locacaoId.trim();
        this.numero = numero;
        this.locadorId = locadorId.trim();
        this.locatarioId = locatarioId.trim();
        this.maquinaId = maquinaId.trim();
        this.nomeLocador = nomeLocador.trim();
        this.nomeLocatario = nomeLocatario.trim();
        this.descricaoMaquina = descricaoMaquina.trim();
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valorTotal = Contrato.normalizar(valorTotal);
        this.valorCaucao = Contrato.normalizar(valorCaucao);
        this.dataEmissao = LocalDateTime.now();
    }

    // ---- Validacoes estaticas (podem ser usadas antes de criar o objeto) ----

    // Texto nao pode ser nulo nem so espacos
    public static boolean isTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    // Numero no formato AM-ANO-00123 (ano com 4 digitos e sequencia com 5)
    public static boolean isNumeroValido(String numero) {
        return numero != null && numero.matches("^" + PREFIXO_NUMERO + "-\\d{4}-\\d{5}$");
    }

    // O fim nao pode vir antes do inicio (os dois dias contam como diaria)
    public static boolean isPeriodoValido(LocalDate inicio, LocalDate fim) {
        return inicio != null && fim != null && !fim.isBefore(inicio);
    }

    // O total da locacao precisa ser maior que zero
    public static boolean isValorTotalValido(BigDecimal valor) {
        return valor != null && valor.signum() > 0;
    }

    // A caucao pode ser zero, mas nunca negativa
    public static boolean isValorCaucaoValido(BigDecimal valor) {
        return valor != null && valor.signum() >= 0;
    }

    // Monta o numero a partir do ano e da sequencia: AM-2026-00123
    public static String montarNumero(int ano, long sequencia) {
        return String.format("%s-%04d-%05d", PREFIXO_NUMERO, ano, sequencia);
    }

    // Deixa todo valor com 2 casas: 850 e 850.00 viram o mesmo numero no equals e no hashCode
    private static BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    // ---- Getters e setters (os setters validam os dados) ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocacaoId() {
        return locacaoId;
    }

    public void setLocacaoId(String locacaoId) throws Exception {
        if (!Contrato.isTextoValido(locacaoId))
            throw new Exception("Locacao ausente");
        this.locacaoId = locacaoId.trim();
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) throws Exception {
        if (!Contrato.isNumeroValido(numero))
            throw new Exception("Numero do contrato invalido");
        this.numero = numero;
    }

    public String getLocadorId() {
        return locadorId;
    }

    public String getLocatarioId() {
        return locatarioId;
    }

    public String getMaquinaId() {
        return maquinaId;
    }

    public String getNomeLocador() {
        return nomeLocador;
    }

    public String getNomeLocatario() {
        return nomeLocatario;
    }

    public String getDescricaoMaquina() {
        return descricaoMaquina;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) throws Exception {
        if (!Contrato.isValorTotalValido(valorTotal))
            throw new Exception("Valor total invalido");
        this.valorTotal = Contrato.normalizar(valorTotal);
    }

    public BigDecimal getValorCaucao() {
        return valorCaucao;
    }

    public void setValorCaucao(BigDecimal valorCaucao) throws Exception {
        if (!Contrato.isValorCaucaoValido(valorCaucao))
            throw new Exception("Valor da caucao invalido");
        this.valorCaucao = Contrato.normalizar(valorCaucao);
    }

    public boolean isAssinadoPeloLocador() {
        return assinadoPeloLocador;
    }

    public boolean isAssinadoPeloLocatario() {
        return assinadoPeloLocatario;
    }

    public LocalDateTime getDataAssinaturaLocador() {
        return dataAssinaturaLocador;
    }

    public LocalDateTime getDataAssinaturaLocatario() {
        return dataAssinaturaLocatario;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    // ---- Metodos que alteram o this (verbo no imperativo) ----

    // O locador assina agora; nao pode assinar duas vezes
    public void assineComoLocador() throws Exception {
        if (this.assinadoPeloLocador)
            throw new Exception("Locador ja assinou");

        this.assinadoPeloLocador = true;
        this.dataAssinaturaLocador = LocalDateTime.now();
    }

    // O locatario assina agora; nao pode assinar duas vezes
    public void assineComoLocatario() throws Exception {
        if (this.assinadoPeloLocatario)
            throw new Exception("Locatario ja assinou");

        this.assinadoPeloLocatario = true;
        this.dataAssinaturaLocatario = LocalDateTime.now();
    }

    // O contrato so vale depois que as duas partes assinaram
    public boolean isVigente() {
        return this.assinadoPeloLocador && this.assinadoPeloLocatario;
    }

    // ---- Metodos canonicos ----

    // Construtor de copia
    public Contrato(Contrato modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.locacaoId = modelo.locacaoId;
        this.numero = modelo.numero;
        this.locadorId = modelo.locadorId;
        this.locatarioId = modelo.locatarioId;
        this.maquinaId = modelo.maquinaId;
        this.nomeLocador = modelo.nomeLocador;
        this.nomeLocatario = modelo.nomeLocatario;
        this.descricaoMaquina = modelo.descricaoMaquina;
        this.dataInicio = modelo.dataInicio;                           // LocalDate e imutavel
        this.dataFim = modelo.dataFim;                                 // LocalDate e imutavel
        this.valorTotal = modelo.valorTotal;                           // BigDecimal e imutavel
        this.valorCaucao = modelo.valorCaucao;                         // BigDecimal e imutavel
        this.assinadoPeloLocador = modelo.assinadoPeloLocador;
        this.assinadoPeloLocatario = modelo.assinadoPeloLocatario;
        this.dataAssinaturaLocador = modelo.dataAssinaturaLocador;     // LocalDateTime e imutavel
        this.dataAssinaturaLocatario = modelo.dataAssinaturaLocatario; // LocalDateTime e imutavel
        this.dataEmissao = modelo.dataEmissao;                         // LocalDateTime e imutavel
    }

    @Override
    public Object clone() {
        Contrato retorno = null;

        try {
            retorno = new Contrato(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return "Contrato " + this.numero
                + " - locacao " + this.locacaoId
                + " - de " + this.dataInicio + " a " + this.dataFim
                + " - total R$ " + this.valorTotal
                + (this.isVigente() ? " - vigente" : " - aguardando assinaturas");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        Contrato con = (Contrato) obj;

        if (!Objects.equals(this.id, con.id)) return false;
        if (!Objects.equals(this.locacaoId, con.locacaoId)) return false;
        if (!Objects.equals(this.numero, con.numero)) return false;
        if (!Objects.equals(this.locadorId, con.locadorId)) return false;
        if (!Objects.equals(this.locatarioId, con.locatarioId)) return false;
        if (!Objects.equals(this.maquinaId, con.maquinaId)) return false;
        if (!Objects.equals(this.nomeLocador, con.nomeLocador)) return false;
        if (!Objects.equals(this.nomeLocatario, con.nomeLocatario)) return false;
        if (!Objects.equals(this.descricaoMaquina, con.descricaoMaquina)) return false;
        if (!Objects.equals(this.dataInicio, con.dataInicio)) return false;
        if (!Objects.equals(this.dataFim, con.dataFim)) return false;
        if (!Objects.equals(this.valorTotal, con.valorTotal)) return false;
        if (!Objects.equals(this.valorCaucao, con.valorCaucao)) return false;
        if (this.assinadoPeloLocador != con.assinadoPeloLocador) return false;
        if (this.assinadoPeloLocatario != con.assinadoPeloLocatario) return false;
        if (!Objects.equals(this.dataAssinaturaLocador, con.dataAssinaturaLocador)) return false;
        if (!Objects.equals(this.dataAssinaturaLocatario, con.dataAssinaturaLocatario)) return false;
        if (!Objects.equals(this.dataEmissao, con.dataEmissao)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.locacaoId == null ? 0 : this.locacaoId.hashCode());
        retorno = retorno * 2 + (this.numero == null ? 0 : this.numero.hashCode());
        retorno = retorno * 2 + (this.locadorId == null ? 0 : this.locadorId.hashCode());
        retorno = retorno * 2 + (this.locatarioId == null ? 0 : this.locatarioId.hashCode());
        retorno = retorno * 2 + (this.maquinaId == null ? 0 : this.maquinaId.hashCode());
        retorno = retorno * 2 + (this.nomeLocador == null ? 0 : this.nomeLocador.hashCode());
        retorno = retorno * 2 + (this.nomeLocatario == null ? 0 : this.nomeLocatario.hashCode());
        retorno = retorno * 2 + (this.descricaoMaquina == null ? 0 : this.descricaoMaquina.hashCode());
        retorno = retorno * 2 + (this.dataInicio == null ? 0 : this.dataInicio.hashCode());
        retorno = retorno * 2 + (this.dataFim == null ? 0 : this.dataFim.hashCode());
        retorno = retorno * 2 + (this.valorTotal == null ? 0 : this.valorTotal.hashCode());
        retorno = retorno * 2 + (this.valorCaucao == null ? 0 : this.valorCaucao.hashCode());
        retorno = retorno * 2 + Boolean.hashCode(this.assinadoPeloLocador);
        retorno = retorno * 2 + Boolean.hashCode(this.assinadoPeloLocatario);
        retorno = retorno * 2 + (this.dataAssinaturaLocador == null ? 0 : this.dataAssinaturaLocador.hashCode());
        retorno = retorno * 2 + (this.dataAssinaturaLocatario == null ? 0 : this.dataAssinaturaLocatario.hashCode());
        retorno = retorno * 2 + (this.dataEmissao == null ? 0 : this.dataEmissao.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    // Ordem natural: pela data de emissao (o contrato mais antigo vem primeiro)
    @Override
    public int compareTo(Contrato con) {
        if (this.dataEmissao == null && con.dataEmissao == null) return 0;
        if (this.dataEmissao == null) return -666;
        if (con.dataEmissao == null) return 666;

        if (this.dataEmissao.isBefore(con.dataEmissao)) return -666;
        if (this.dataEmissao.isAfter(con.dataEmissao)) return 666;
        return 0;
    }
}
