/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Entidade VistoriaChecklist (collection "vistorias_checklist") com os itens conferidos na retirada ou na devolucao
*/

package com.alugamaquina.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vistorias_checklist")
// Uma vistoria de cada tipo por locacao, garantido pelo banco mesmo com dois pedidos ao mesmo tempo
@CompoundIndex(name = "locacao_tipo_unico", def = "{'locacaoId': 1, 'tipo': 1}", unique = true)
public class VistoriaChecklist implements Comparable<VistoriaChecklist>, Cloneable {

    @Id
    private String id;

    private String locacaoId;
    private String maquinaId;
    private TipoVistoria tipo;

    // Lista de objetos mutaveis: o construtor e o de copia fazem copia profunda
    private List<ItemChecklist> itens;

    // Horas de uso do motor, informadas ao concluir
    private Double horimetro;

    private String responsavelId;
    private LocalDateTime dataVistoria;
    private StatusVistoria status;

    // Construtor vazio: o Spring Data usa para ler do banco
    public VistoriaChecklist() {
    }

    // Vistoria nova: nasce PENDENTE, com todos os itens ainda sem resposta
    public VistoriaChecklist(String locacaoId, String maquinaId, TipoVistoria tipo,
                             String responsavelId, List<ItemChecklist> itens) throws Exception {
        if (!VistoriaChecklist.isTextoValido(locacaoId))
            throw new Exception("Locacao ausente");
        if (!VistoriaChecklist.isTextoValido(maquinaId))
            throw new Exception("Maquina ausente");
        if (tipo == null)
            throw new Exception("Tipo da vistoria ausente");
        if (!VistoriaChecklist.isTextoValido(responsavelId))
            throw new Exception("Responsavel ausente");
        if (itens == null || itens.isEmpty())
            throw new Exception("Itens do checklist ausentes");

        this.locacaoId = locacaoId.trim();
        this.maquinaId = maquinaId.trim();
        this.tipo = tipo;
        this.responsavelId = responsavelId.trim();
        this.itens = VistoriaChecklist.copiarItens(itens);
        this.status = StatusVistoria.PENDENTE;
        this.dataVistoria = LocalDateTime.now();
    }

    // ---- Validacoes estaticas ----

    // Texto nao pode ser nulo nem so espacos
    public static boolean isTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    // O horimetro nao pode ser negativo (zero e aceito: maquina nova)
    public static boolean isHorimetroValido(Double horimetro) {
        return horimetro != null && !horimetro.isNaN() && !horimetro.isInfinite() && horimetro >= 0;
    }

    // Copia profunda: cada item da lista e clonado
    private static List<ItemChecklist> copiarItens(List<ItemChecklist> itens) throws Exception {
        List<ItemChecklist> copia = new ArrayList<>();
        for (ItemChecklist item : itens) {
            if (item == null)
                throw new Exception("Item do checklist ausente");
            copia.add((ItemChecklist) item.clone());
        }
        return copia;
    }

    // ---- Getters e setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocacaoId() {
        return locacaoId;
    }

    public String getMaquinaId() {
        return maquinaId;
    }

    public TipoVistoria getTipo() {
        return tipo;
    }

    // Devolve uma copia da lista, para quem chamar nao mexer nos itens por fora
    public List<ItemChecklist> getItens() {
        try {
            return this.itens == null ? null : VistoriaChecklist.copiarItens(this.itens);
        } catch (Exception erro) {
            // sei que nao vai dar erro: a lista guardada nunca tem item nulo
            return null;
        }
    }

    public Double getHorimetro() {
        return horimetro;
    }

    public String getResponsavelId() {
        return responsavelId;
    }

    public LocalDateTime getDataVistoria() {
        return dataVistoria;
    }

    public void setDataVistoria(LocalDateTime dataVistoria) {
        this.dataVistoria = dataVistoria;
    }

    public StatusVistoria getStatus() {
        return status;
    }

    // ---- Metodos que alteram o this (verbo no imperativo) ----

    // Registra a resposta de um item pela descricao; so enquanto a vistoria esta PENDENTE
    public void respondaItem(String descricao, boolean conforme, String observacao) throws Exception {
        if (this.status != StatusVistoria.PENDENTE)
            throw new Exception("Vistoria ja concluida");

        for (ItemChecklist item : this.itens) {
            if (item.getDescricao().equals(descricao == null ? null : descricao.trim())) {
                item.responda(conforme, observacao);
                return;
            }
        }
        throw new Exception("Item nao existe no checklist: " + descricao);
    }

    // Conclui a vistoria: todos os itens respondidos e horimetro valido; se algum item nao esta conforme, COM_PENDENCIAS
    public void conclua(Double horimetro) throws Exception {
        if (this.status != StatusVistoria.PENDENTE)
            throw new Exception("Vistoria ja concluida");
        if (!VistoriaChecklist.isHorimetroValido(horimetro))
            throw new Exception("Horimetro invalido");
        if (!this.isTodosRespondidos())
            throw new Exception("Responda todos os itens do checklist");

        this.horimetro = horimetro;
        this.status = this.temPendencias() ? StatusVistoria.COM_PENDENCIAS : StatusVistoria.CONCLUIDA;
    }

    // ---- Predicativas ----

    public boolean isTodosRespondidos() {
        for (ItemChecklist item : this.itens) {
            if (!item.isRespondido())
                return false;
        }
        return true;
    }

    public boolean temPendencias() {
        for (ItemChecklist item : this.itens) {
            if (item.isNaoConforme())
                return true;
        }
        return false;
    }

    public boolean isConcluida() {
        return this.status == StatusVistoria.CONCLUIDA || this.status == StatusVistoria.COM_PENDENCIAS;
    }

    // ---- Metodos canonicos ----

    // Construtor de copia (com copia profunda dos itens)
    public VistoriaChecklist(VistoriaChecklist modelo) throws Exception {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.id = modelo.id;
        this.locacaoId = modelo.locacaoId;
        this.maquinaId = modelo.maquinaId;
        this.tipo = modelo.tipo;
        this.itens = modelo.itens == null ? null : VistoriaChecklist.copiarItens(modelo.itens);
        this.horimetro = modelo.horimetro;          // Double e imutavel
        this.responsavelId = modelo.responsavelId;
        this.dataVistoria = modelo.dataVistoria;    // LocalDateTime e imutavel
        this.status = modelo.status;
    }

    @Override
    public Object clone() {
        VistoriaChecklist retorno = null;

        try {
            retorno = new VistoriaChecklist(this);
        } catch (Exception erro) {
            // sei que nao vai dar erro: o this nunca e nulo
        }

        return retorno;
    }

    @Override
    public String toString() {
        return "Vistoria " + this.tipo + " da locacao " + this.locacaoId
                + " - " + (this.itens == null ? 0 : this.itens.size()) + " itens"
                + " - " + this.status
                + (this.horimetro == null ? "" : " - horimetro " + this.horimetro + " h");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        VistoriaChecklist vis = (VistoriaChecklist) obj;

        if (!Objects.equals(this.id, vis.id)) return false;
        if (!Objects.equals(this.locacaoId, vis.locacaoId)) return false;
        if (!Objects.equals(this.maquinaId, vis.maquinaId)) return false;
        if (this.tipo != vis.tipo) return false;
        if (!Objects.equals(this.itens, vis.itens)) return false;
        if (!Objects.equals(this.horimetro, vis.horimetro)) return false;
        if (!Objects.equals(this.responsavelId, vis.responsavelId)) return false;
        if (!Objects.equals(this.dataVistoria, vis.dataVistoria)) return false;
        if (this.status != vis.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int retorno = 1;

        retorno = retorno * 2 + (this.id == null ? 0 : this.id.hashCode());
        retorno = retorno * 2 + (this.locacaoId == null ? 0 : this.locacaoId.hashCode());
        retorno = retorno * 2 + (this.maquinaId == null ? 0 : this.maquinaId.hashCode());
        retorno = retorno * 2 + (this.tipo == null ? 0 : this.tipo.name().hashCode());
        retorno = retorno * 2 + (this.itens == null ? 0 : this.itens.hashCode());
        retorno = retorno * 2 + (this.horimetro == null ? 0 : this.horimetro.hashCode());
        retorno = retorno * 2 + (this.responsavelId == null ? 0 : this.responsavelId.hashCode());
        retorno = retorno * 2 + (this.dataVistoria == null ? 0 : this.dataVistoria.hashCode());
        retorno = retorno * 2 + (this.status == null ? 0 : this.status.name().hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    // Ordem natural: pela data da vistoria (a mais antiga vem primeiro)
    @Override
    public int compareTo(VistoriaChecklist vis) {
        if (this.dataVistoria == null && vis.dataVistoria == null) return 0;
        if (this.dataVistoria == null) return -666;
        if (vis.dataVistoria == null) return 666;

        if (this.dataVistoria.isBefore(vis.dataVistoria)) return -666;
        if (this.dataVistoria.isAfter(vis.dataVistoria)) return 666;
        return 0;
    }
}
