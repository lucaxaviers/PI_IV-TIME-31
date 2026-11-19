/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: DTO de resposta com a vistoria tecnica, os itens do checklist e o estado final
*/

package com.alugamaquina.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.alugamaquina.backend.model.StatusVistoria;
import com.alugamaquina.backend.model.TipoVistoria;

public class VistoriaResponse {

    private final boolean sucesso;
    private final String mensagem;
    private final String id;
    private final String locacaoId;
    private final String maquinaId;
    private final TipoVistoria tipo;
    private final List<ItemChecklistDto> itens;
    private final Double horimetro;
    private final String responsavelId;
    private final LocalDateTime dataVistoria;
    private final StatusVistoria status;
    private final boolean temPendencias;

    public VistoriaResponse(boolean sucesso, String mensagem, String id, String locacaoId, String maquinaId,
                            TipoVistoria tipo, List<ItemChecklistDto> itens, Double horimetro,
                            String responsavelId, LocalDateTime dataVistoria, StatusVistoria status,
                            boolean temPendencias) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.id = id;
        this.locacaoId = locacaoId;
        this.maquinaId = maquinaId;
        this.tipo = tipo;
        this.itens = itens;
        this.horimetro = horimetro;
        this.responsavelId = responsavelId;
        this.dataVistoria = dataVistoria;
        this.status = status;
        this.temPendencias = temPendencias;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getId() {
        return id;
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

    public List<ItemChecklistDto> getItens() {
        return itens;
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

    public StatusVistoria getStatus() {
        return status;
    }

    public boolean isTemPendencias() {
        return temPendencias;
    }
}
