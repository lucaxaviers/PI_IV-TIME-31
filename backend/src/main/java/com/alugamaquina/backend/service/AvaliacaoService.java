/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Servico de regras de avaliacao pos-locacao e calculo de reputacao
*/

package com.alugamaquina.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.AvaliacaoRequest;
import com.alugamaquina.backend.dto.AvaliacaoResponse;
import com.alugamaquina.backend.model.Avaliacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.AvaliacaoRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

/**
 * Servico responsavel pelas regras de negocio de avaliacao pos-locacao
 * e pelo recalculo automatico da reputacao media do locador no MongoDB.
 *
 * Responsavel: Lucas Rodrigues Xavier | 25000508 (Issue #45)
 */
@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, UsuarioRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra uma avaliacao pos-locacao e recalcula a reputacao media do locador.
     */
    public AvaliacaoResponse registrarAvaliacao(AvaliacaoRequest request) {
        validarRequest(request);

        // Calcula a media das 5 notas individuais (escala de 1 a 5)
        double somaNotas = request.getNotaDesempenho()
                + request.getNotaConservacao()
                + request.getNotaFidelidade()
                + request.getNotaPontualidade()
                + request.getNotaSuporte();

        double mediaIndividual = arredondar(somaNotas / 5.0);

        Avaliacao avaliacao = new Avaliacao(
                request.getLocacaoId(),
                request.getLocadorId(),
                request.getLocatarioId(),
                request.getMaquinaId(),
                request.getNotaDesempenho(),
                request.getNotaConservacao(),
                request.getNotaFidelidade(),
                request.getNotaPontualidade(),
                request.getNotaSuporte(),
                mediaIndividual,
                request.getComentario() != null ? request.getComentario().trim() : "",
                request.isRecomenda()
        );

        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        // Recalculo da reputacao media do locador considerando todo o historico
        double novaReputacao = recalcularReputacaoLocador(request.getLocadorId());
        int totalAvaliacoes = avaliacaoRepository.findByLocadorId(request.getLocadorId()).size();

        return new AvaliacaoResponse(
                true,
                "Avaliacao registrada com sucesso e reputacao recalculada!",
                salva.getId(),
                salva.getLocacaoId(),
                salva.getLocadorId(),
                mediaIndividual,
                novaReputacao,
                totalAvaliacoes,
                salva.getDataAvaliacao()
        );
    }

    /**
     * Recalcula a media aritmetica de todas as avaliacoes recebidas por um locador
     * e persiste a nova nota no perfil do usuario.
     */
    public double recalcularReputacaoLocador(String locadorId) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByLocadorId(locadorId);

        if (avaliacoes.isEmpty()) {
            return 5.0; // Reputacao inicial neutra
        }

        double somaMedias = 0.0;
        for (Avaliacao a : avaliacoes) {
            somaMedias += a.getMediaFinal();
        }

        double reputacaoFinal = arredondar(somaMedias / avaliacoes.size());

        // Se o locador existir no banco, atualiza o perfil dele
        Optional<Usuario> optLocador = usuarioRepository.findById(locadorId);
        if (optLocador.isPresent()) {
            Usuario locador = optLocador.get();
            locador.setReputacaoMedia(reputacaoFinal);
            locador.setTotalAvaliacoes(avaliacoes.size());
            usuarioRepository.save(locador);
        }

        return reputacaoFinal;
    }

    public List<Avaliacao> listarPorLocador(String locadorId) {
        return avaliacaoRepository.findByLocadorId(locadorId);
    }

    private void validarRequest(AvaliacaoRequest request) {
        if (request.getLocacaoId() == null || request.getLocacaoId().trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador da locacao e obrigatorio.");
        }
        if (request.getLocadorId() == null || request.getLocadorId().trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador do locador e obrigatorio.");
        }

        validarNota("Desempenho", request.getNotaDesempenho());
        validarNota("Conservacao", request.getNotaConservacao());
        validarNota("Fidelidade ao anuncio", request.getNotaFidelidade());
        validarNota("Pontualidade", request.getNotaPontualidade());
        validarNota("Suporte tecnico", request.getNotaSuporte());
    }

    private void validarNota(String criterio, int nota) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota de " + criterio + " deve estar entre 1 e 5 estrelas.");
        }
    }

    private double arredondar(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
