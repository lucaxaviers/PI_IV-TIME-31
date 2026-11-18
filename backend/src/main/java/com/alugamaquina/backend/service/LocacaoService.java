/**
 * AlugaMáquina - Projeto Integrador IV (Time 31, PUC-Campinas)
 * Módulo: Solicitação e Aceite de Locações e Frete
 * Autor: Tiago Medeiros | RA: 25000845
 * Descrição: Regras de negócio da locação: validação, diárias, frete, conflito de agenda e
 *            transições de status (PENDENTE para ACEITO ou RECUSADO).
 */

package com.alugamaquina.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.LocacaoResponse;
import com.alugamaquina.backend.dto.SolicitacaoLocacaoRequest;
import com.alugamaquina.backend.exception.ConflitoAgendaException;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.exception.TransicaoStatusInvalidaException;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.repository.LocacaoRepository;

@Service
public class LocacaoService {

    // Mesma regra do frete.js do front-end: km x 2 (ida e volta) x valor por km + taxa fixa
    public static final BigDecimal VALOR_POR_KM = new BigDecimal("6.50");
    public static final BigDecimal TAXA_MOBILIZACAO = new BigDecimal("150.00");

    private final LocacaoRepository locacaoRepository;

    public LocacaoService(LocacaoRepository locacaoRepository) {
        this.locacaoRepository = locacaoRepository;
    }

    /**
     * Registra a solicitacao do locatario como PENDENTE, calculando diarias, frete e total.
     * Bloqueia se a maquina ja tem reserva ACEITA no periodo.
     */
    public LocacaoResponse solicitar(SolicitacaoLocacaoRequest request) {
        validar(request);

        if (existeReservaAceita(request.getMaquinaId().trim(), request.getDataInicio(), request.getDataFim())) {
            throw new ConflitoAgendaException("A maquina ja possui reserva aceita neste periodo.");
        }

        long diarias = calcularDiarias(request.getDataInicio(), request.getDataFim());
        BigDecimal valorDiaria = request.getValorDiaria().setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorFrete = calcularFrete(request.getDistanciaKm());
        BigDecimal valorTotal = valorDiaria.multiply(BigDecimal.valueOf(diarias))
                .add(valorFrete)
                .setScale(2, RoundingMode.HALF_UP);

        Locacao locacao = new Locacao(
                request.getLocadorId().trim(),
                request.getLocatarioId().trim(),
                request.getMaquinaId().trim(),
                request.getDataInicio(),
                request.getDataFim(),
                valorDiaria,
                valorFrete,
                valorTotal
        );

        Locacao salva = locacaoRepository.save(locacao);

        return montarResposta("Solicitacao enviada! Aguarde a resposta do locador.", salva);
    }

    /**
     * O locador aceita uma solicitacao PENDENTE. Confere de novo a agenda, porque outra
     * reserva pode ter sido aceita depois que esta foi pedida.
     */
    public LocacaoResponse aceitar(String id) {
        Locacao locacao = buscar(id);
        exigirPendente(locacao, StatusLocacao.ACEITO);

        if (existeReservaAceita(locacao.getMaquinaId(), locacao.getDataInicio(), locacao.getDataFim())) {
            throw new ConflitoAgendaException("A maquina ja possui reserva aceita neste periodo.");
        }

        locacao.setStatus(StatusLocacao.ACEITO);
        Locacao salva = locacaoRepository.save(locacao);

        return montarResposta("Locacao aceita com sucesso.", salva);
    }

    /**
     * O locador recusa uma solicitacao PENDENTE. O motivo e opcional.
     */
    public LocacaoResponse recusar(String id, String motivo) {
        Locacao locacao = buscar(id);
        exigirPendente(locacao, StatusLocacao.RECUSADO);

        locacao.setStatus(StatusLocacao.RECUSADO);
        locacao.setMotivoRecusa(motivo == null ? "" : motivo.trim());
        Locacao salva = locacaoRepository.save(locacao);

        return montarResposta("Locacao recusada.", salva);
    }

    /**
     * Numero de diarias entre as duas datas, contando o primeiro e o ultimo dia
     * (mesmo calculo do front-end: 10/11 a 14/11 sao 5 diarias).
     */
    public static long calcularDiarias(LocalDate inicio, LocalDate fim) {
        return ChronoUnit.DAYS.between(inicio, fim) + 1;
    }

    /**
     * Frete estimado: km x 2 (ida e volta) x R$ 6,50 + taxa fixa de R$ 150,00.
     * Com 0 km (retirada no local) nao ha frete.
     */
    public static BigDecimal calcularFrete(double distanciaKm) {
        if (Double.isNaN(distanciaKm) || Double.isInfinite(distanciaKm) || distanciaKm < 0) {
            throw new IllegalArgumentException("A distancia do frete deve ser zero ou positiva.");
        }
        if (distanciaKm == 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return BigDecimal.valueOf(distanciaKm)
                .multiply(BigDecimal.valueOf(2))
                .multiply(VALOR_POR_KM)
                .add(TAXA_MOBILIZACAO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Procura reserva ACEITA da maquina que cruze com o periodo
    private boolean existeReservaAceita(String maquinaId, LocalDate inicio, LocalDate fim) {
        return locacaoRepository
                .existsByMaquinaIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                        maquinaId, StatusLocacao.ACEITO, fim, inicio);
    }

    private Locacao buscar(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador da locacao e obrigatorio.");
        }
        return locacaoRepository.findById(id.trim())
                .orElseThrow(() -> new LocacaoNaoEncontradaException("Locacao nao encontrada: " + id.trim()));
    }

    // So PENDENTE pode virar ACEITO ou RECUSADO
    private void exigirPendente(Locacao locacao, StatusLocacao destino) {
        if (locacao.getStatus() != StatusLocacao.PENDENTE) {
            throw new TransicaoStatusInvalidaException(
                    "Transicao invalida: de " + locacao.getStatus() + " para " + destino + ".");
        }
    }

    // Ids, datas e valor sao obrigatorios; o fim nao pode vir antes do inicio
    private void validar(SolicitacaoLocacaoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados da solicitacao sao obrigatorios.");
        }
        exigirTexto(request.getLocadorId(), "Identificador do locador");
        exigirTexto(request.getLocatarioId(), "Identificador do locatario");
        exigirTexto(request.getMaquinaId(), "Identificador da maquina");

        if (request.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de inicio e obrigatoria.");
        }
        if (request.getDataFim() == null) {
            throw new IllegalArgumentException("Data de termino e obrigatoria.");
        }
        if (request.getDataFim().isBefore(request.getDataInicio())) {
            throw new IllegalArgumentException("A data de termino nao pode ser anterior a data de inicio.");
        }
        if (request.getValorDiaria() == null || request.getValorDiaria().signum() <= 0) {
            throw new IllegalArgumentException("O valor da diaria deve ser maior que zero.");
        }
    }

    private void exigirTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " e obrigatorio.");
        }
    }

    private LocacaoResponse montarResposta(String mensagem, Locacao locacao) {
        return new LocacaoResponse(
                true,
                mensagem,
                locacao.getId(),
                locacao.getMaquinaId(),
                locacao.getStatus(),
                locacao.getDataInicio(),
                locacao.getDataFim(),
                calcularDiarias(locacao.getDataInicio(), locacao.getDataFim()),
                locacao.getValorDiaria(),
                locacao.getValorFrete(),
                locacao.getValorTotal(),
                locacao.getMotivoRecusa()
        );
    }
}
