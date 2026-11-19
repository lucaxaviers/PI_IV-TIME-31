/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Regras do contrato digital: gerar a partir de locacao aceita, assinar e gerar o PDF
*/

package com.alugamaquina.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.ContratoResponse;
import com.alugamaquina.backend.exception.AssinaturaNaoPermitidaException;
import com.alugamaquina.backend.exception.ContratoConflitoException;
import com.alugamaquina.backend.exception.ContratoNaoEncontradoException;
import com.alugamaquina.backend.exception.LocacaoNaoEncontradaException;
import com.alugamaquina.backend.model.Contrato;
import com.alugamaquina.backend.model.Locacao;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.model.StatusLocacao;
import com.alugamaquina.backend.model.Usuario;
import com.alugamaquina.backend.repository.ContratoRepository;
import com.alugamaquina.backend.repository.LocacaoRepository;
import com.alugamaquina.backend.repository.MaquinaRepository;
import com.alugamaquina.backend.repository.UsuarioRepository;

@Service
public class ContratoService {

    // Mesma caucao mostrada na tela do contrato
    public static final BigDecimal VALOR_CAUCAO_PADRAO = new BigDecimal("1000.00");

    private final ContratoRepository contratoRepository;
    private final LocacaoRepository locacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MaquinaRepository maquinaRepository;
    private final GeradorPdfContrato geradorPdfContrato;

    public ContratoService(ContratoRepository contratoRepository,
                           LocacaoRepository locacaoRepository,
                           UsuarioRepository usuarioRepository,
                           MaquinaRepository maquinaRepository,
                           GeradorPdfContrato geradorPdfContrato) {
        this.contratoRepository = contratoRepository;
        this.locacaoRepository = locacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.maquinaRepository = maquinaRepository;
        this.geradorPdfContrato = geradorPdfContrato;
    }

    /**
     * Gera o contrato de uma locacao ACEITA. Cada locacao tem no maximo um contrato.
     */
    public ContratoResponse gerar(String locacaoId) {
        exigirTexto(locacaoId, "Identificador da locacao");
        String idLocacao = locacaoId.trim();

        Locacao locacao = locacaoRepository.findById(idLocacao)
                .orElseThrow(() -> new LocacaoNaoEncontradaException("Locacao nao encontrada: " + idLocacao));

        if (locacao.getStatus() != StatusLocacao.ACEITO) {
            throw new ContratoConflitoException(
                    "So locacao aceita pode ter contrato. Status atual: " + locacao.getStatus() + ".");
        }
        if (contratoRepository.existsByLocacaoId(idLocacao)) {
            throw new ContratoConflitoException("Esta locacao ja tem contrato.");
        }

        Usuario locador = usuarioRepository.findById(locacao.getLocadorId())
                .orElseThrow(() -> new IllegalArgumentException("Locador nao encontrado: " + locacao.getLocadorId()));
        Usuario locatario = usuarioRepository.findById(locacao.getLocatarioId())
                .orElseThrow(() -> new IllegalArgumentException("Locatario nao encontrado: " + locacao.getLocatarioId()));
        Maquina maquina = maquinaRepository.findById(locacao.getMaquinaId())
                .orElseThrow(() -> new IllegalArgumentException("Maquina nao encontrada: " + locacao.getMaquinaId()));

        String numero = Contrato.montarNumero(LocalDate.now().getYear(), contratoRepository.count() + 1);

        Contrato contrato;
        try {
            contrato = new Contrato(idLocacao, numero, locacao.getLocadorId(), locacao.getLocatarioId(),
                    locacao.getMaquinaId(), locador.getNome(), locatario.getNome(), maquina.getTituloAnuncio(),
                    locacao.getDataInicio(), locacao.getDataFim(), locacao.getValorTotal(), VALOR_CAUCAO_PADRAO);
        } catch (Exception erro) {
            throw new IllegalArgumentException(erro.getMessage());
        }

        return montarResposta("Contrato gerado com sucesso.", contratoRepository.save(contrato));
    }

    public ContratoResponse buscar(String id) {
        return montarResposta("Contrato encontrado.", buscarContrato(id));
    }

    // A tela chama primeiro por aqui: se ja existe contrato da locacao, mostra o que existe
    public ContratoResponse buscarPorLocacao(String locacaoId) {
        exigirTexto(locacaoId, "Identificador da locacao");
        Contrato contrato = contratoRepository.findByLocacaoId(locacaoId.trim())
                .orElseThrow(() -> new ContratoNaoEncontradoException(
                        "Nao ha contrato para a locacao: " + locacaoId.trim()));
        return montarResposta("Contrato encontrado.", contrato);
    }

    /**
     * Registra a assinatura do usuario. So o locador e o locatario do contrato podem assinar,
     * e cada um assina uma vez.
     */
    public ContratoResponse assinar(String id, String usuarioId) {
        exigirTexto(usuarioId, "Identificador do usuario");
        String idUsuario = usuarioId.trim();
        Contrato contrato = buscarContrato(id);

        boolean ehLocador = idUsuario.equals(contrato.getLocadorId());
        boolean ehLocatario = idUsuario.equals(contrato.getLocatarioId());

        if (!ehLocador && !ehLocatario) {
            throw new AssinaturaNaoPermitidaException("Somente o locador ou o locatario podem assinar este contrato.");
        }

        try {
            // Se a mesma pessoa for as duas partes, assina primeiro como locador e depois como locatario
            if (ehLocador && !contrato.isAssinadoPeloLocador()) {
                contrato.assineComoLocador();
            } else if (ehLocatario) {
                contrato.assineComoLocatario();
            } else {
                contrato.assineComoLocador();
            }
        } catch (Exception erro) {
            throw new ContratoConflitoException(erro.getMessage());
        }

        Contrato salvo = contratoRepository.save(contrato);
        String mensagem = salvo.isVigente()
                ? "Contrato assinado pelas duas partes."
                : "Assinatura registrada. Aguardando a outra parte.";
        return montarResposta(mensagem, salvo);
    }

    // Devolve os bytes do PDF do contrato
    public byte[] gerarPdf(String id) {
        return geradorPdfContrato.gerar(buscarContrato(id));
    }

    private Contrato buscarContrato(String id) {
        exigirTexto(id, "Identificador do contrato");
        return contratoRepository.findById(id.trim())
                .orElseThrow(() -> new ContratoNaoEncontradoException("Contrato nao encontrado: " + id.trim()));
    }

    private void exigirTexto(String valor, String campo) {
        if (!Contrato.isTextoValido(valor)) {
            throw new IllegalArgumentException(campo + " e obrigatorio.");
        }
    }

    private ContratoResponse montarResposta(String mensagem, Contrato contrato) {
        return new ContratoResponse(
                true,
                mensagem,
                contrato.getId(),
                contrato.getLocacaoId(),
                contrato.getNumero(),
                contrato.getNomeLocador(),
                contrato.getNomeLocatario(),
                contrato.getDescricaoMaquina(),
                contrato.getDataInicio(),
                contrato.getDataFim(),
                ChronoUnit.DAYS.between(contrato.getDataInicio(), contrato.getDataFim()) + 1,
                contrato.getValorTotal(),
                contrato.getValorCaucao(),
                contrato.isAssinadoPeloLocador(),
                contrato.isAssinadoPeloLocatario(),
                contrato.isVigente(),
                contrato.getDataEmissao()
        );
    }
}
