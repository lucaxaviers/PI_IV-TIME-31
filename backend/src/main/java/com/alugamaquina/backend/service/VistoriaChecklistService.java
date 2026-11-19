/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Regras da vistoria tecnica: abrir para locacao aceita, responder o checklist e concluir
*/

package com.alugamaquina.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.ConcluirVistoriaRequest;
import com.alugamaquina.backend.dto.CriarVistoriaRequest;
import com.alugamaquina.backend.dto.ItemChecklistDto;
import com.alugamaquina.backend.dto.VistoriaResponse;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.VistoriaConflitoException;
import com.alugamaquina.backend.exception.VistoriaNaoEncontradaException;
import com.alugamaquina.backend.model.ItemChecklist;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.model.VistoriaChecklist;
import com.alugamaquina.backend.repository.LocacaoRepository;
import com.alugamaquina.backend.repository.VistoriaChecklistRepository;

@Service
public class VistoriaChecklistService {

    // Itens que toda vistoria traz
    public static final List<String> ITENS_PADRAO = List.of(
            "Pneus, rodas ou esteiras sem avarias",
            "Nivel de oleo do motor",
            "Nivel de fluidos (arrefecimento e hidraulico)",
            "Luzes e sinalizacao funcionando",
            "Freios funcionando",
            "Cabine, vidros e bancos em bom estado",
            "Implementos acoplados conferidos",
            "Avarias visiveis (riscos, amassados, vazamentos)"
    );

    private final VistoriaChecklistRepository vistoriaRepository;
    private final LocacaoRepository locacaoRepository;

    public VistoriaChecklistService(VistoriaChecklistRepository vistoriaRepository,
                                    LocacaoRepository locacaoRepository) {
        this.vistoriaRepository = vistoriaRepository;
        this.locacaoRepository = locacaoRepository;
    }

    /**
     * Abre a vistoria (retirada ou devolucao) de uma locacao ACEITA, com os itens padrao sem resposta.
     * Cada tipo so pode ser aberto uma vez por locacao, e quem abre precisa ser locador ou locatario.
     */
    public VistoriaResponse abrir(CriarVistoriaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados da vistoria sao obrigatorios.");
        }
        exigirTexto(request.getLocacaoId(), "Identificador da locacao");
        exigirTexto(request.getResponsavelId(), "Identificador do responsavel");
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("Tipo da vistoria e obrigatorio.");
        }

        String locacaoId = request.getLocacaoId().trim();
        String responsavelId = request.getResponsavelId().trim();

        Locacao locacao = locacaoRepository.findById(locacaoId)
                .orElseThrow(() -> new LocacaoNaoEncontradaException("Locacao nao encontrada: " + locacaoId));

        if (locacao.getStatus() != StatusLocacao.ACEITO) {
            throw new VistoriaConflitoException(
                    "So locacao aceita pode ter vistoria. Status atual: " + locacao.getStatus() + ".");
        }
        if (!responsavelId.equals(locacao.getLocadorId()) && !responsavelId.equals(locacao.getLocatarioId())) {
            throw new IllegalArgumentException("O responsavel precisa ser o locador ou o locatario da locacao.");
        }
        if (vistoriaRepository.existsByLocacaoIdAndTipo(locacaoId, request.getTipo())) {
            throw new VistoriaConflitoException(
                    "Esta locacao ja tem vistoria de " + request.getTipo() + ".");
        }

        VistoriaChecklist vistoria;
        try {
            vistoria = new VistoriaChecklist(locacaoId, locacao.getMaquinaId(), request.getTipo(),
                    responsavelId, criarItensPadrao());
        } catch (Exception erro) {
            throw new IllegalArgumentException(erro.getMessage());
        }

        return montarResposta("Vistoria aberta. Preencha o checklist.", vistoriaRepository.save(vistoria));
    }

    /**
     * Registra as respostas e conclui a vistoria. Se algum dado for invalido, nada e salvo:
     * as respostas sao aplicadas primeiro em uma copia.
     */
    public VistoriaResponse concluir(String id, ConcluirVistoriaRequest request) {
        VistoriaChecklist vistoria = buscarVistoria(id);

        if (vistoria.isConcluida()) {
            throw new VistoriaConflitoException("Esta vistoria ja foi concluida.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Dados da conclusao sao obrigatorios.");
        }
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new IllegalArgumentException("Responda os itens do checklist.");
        }

        VistoriaChecklist copia = (VistoriaChecklist) vistoria.clone();
        try {
            for (ItemChecklistDto resposta : request.getItens()) {
                if (resposta == null || resposta.getConforme() == null) {
                    throw new Exception("Todos os itens precisam ser marcados como conforme ou nao conforme");
                }
                copia.respondaItem(resposta.getDescricao(), resposta.getConforme(), resposta.getObservacao());
            }
            copia.conclua(request.getHorimetro());
        } catch (Exception erro) {
            throw new IllegalArgumentException(erro.getMessage());
        }

        VistoriaChecklist salva = vistoriaRepository.save(copia);
        String mensagem = salva.temPendencias()
                ? "Vistoria concluida com pendencias."
                : "Vistoria concluida sem pendencias.";
        return montarResposta(mensagem, salva);
    }

    public VistoriaResponse buscar(String id) {
        return montarResposta("Vistoria encontrada.", buscarVistoria(id));
    }

    // Vistorias de uma locacao (a tela usa para mostrar retirada e devolucao)
    public List<VistoriaResponse> listarPorLocacao(String locacaoId) {
        exigirTexto(locacaoId, "Identificador da locacao");

        List<VistoriaResponse> respostas = new ArrayList<>();
        for (VistoriaChecklist vistoria : vistoriaRepository.findByLocacaoId(locacaoId.trim())) {
            respostas.add(montarResposta("Vistoria encontrada.", vistoria));
        }
        return respostas;
    }

    private List<ItemChecklist> criarItensPadrao() throws Exception {
        List<ItemChecklist> itens = new ArrayList<>();
        for (String descricao : ITENS_PADRAO) {
            itens.add(new ItemChecklist(descricao));
        }
        return itens;
    }

    private VistoriaChecklist buscarVistoria(String id) {
        exigirTexto(id, "Identificador da vistoria");
        return vistoriaRepository.findById(id.trim())
                .orElseThrow(() -> new VistoriaNaoEncontradaException("Vistoria nao encontrada: " + id.trim()));
    }

    private void exigirTexto(String valor, String campo) {
        if (!VistoriaChecklist.isTextoValido(valor)) {
            throw new IllegalArgumentException(campo + " e obrigatorio.");
        }
    }

    private VistoriaResponse montarResposta(String mensagem, VistoriaChecklist vistoria) {
        List<ItemChecklistDto> itens = new ArrayList<>();
        for (ItemChecklist item : vistoria.getItens()) {
            itens.add(new ItemChecklistDto(item.getDescricao(), item.getConforme(), item.getObservacao()));
        }

        return new VistoriaResponse(
                true,
                mensagem,
                vistoria.getId(),
                vistoria.getLocacaoId(),
                vistoria.getMaquinaId(),
                vistoria.getTipo(),
                itens,
                vistoria.getHorimetro(),
                vistoria.getResponsavelId(),
                vistoria.getDataVistoria(),
                vistoria.getStatus(),
                vistoria.temPendencias()
        );
    }
}
