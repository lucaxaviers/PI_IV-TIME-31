/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Regras do cadastro de maquinas: valida os dados do formulario e grava o equipamento no MongoDB
*/

package com.alugamaquina.backend.service;

import java.math.RoundingMode;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.MaquinaCadastroRequest;
import com.alugamaquina.backend.dto.MaquinaCadastroResponse;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.model.MunicipioAtendido;
import com.alugamaquina.backend.model.TipoEquipamento;
import com.alugamaquina.backend.repository.MaquinaRepository;

@Service
public class MaquinaCadastroService {

    private final MaquinaRepository maquinaRepository;

    public MaquinaCadastroService(MaquinaRepository maquinaRepository) {
        this.maquinaRepository = maquinaRepository;
    }

    /**
     * Cadastra a maquina anunciada. O municipio vira o nome de exibicao e as coordenadas
     * (para a busca por raio), e o tipo vira a chave que o catalogo usa (ex.: trator -> tratores).
     */
    public MaquinaCadastroResponse cadastrar(MaquinaCadastroRequest request) {
        validar(request);

        TipoEquipamento tipo = TipoEquipamento.buscarPorCodigo(request.getTipoEquipamento());
        MunicipioAtendido municipio = MunicipioAtendido.buscarPorCodigo(request.getMunicipio());

        Maquina maquina = new Maquina(
                request.getTituloAnuncio().trim(),
                tipo.getChaveCatalogo(),
                municipio.getNome(),
                request.getValorDiaria().setScale(2, RoundingMode.HALF_UP),
                new GeoJsonPoint(municipio.getLongitude(), municipio.getLatitude())
        );

        Maquina salva = maquinaRepository.save(maquina);

        return new MaquinaCadastroResponse(
                true,
                "Equipamento cadastrado! Ele ja aparece no catalogo.",
                salva.getId(),
                salva.getTituloAnuncio(),
                salva.getTipoEquipamento(),
                salva.getMunicipio(),
                salva.getValorDiaria()
        );
    }

    // Confere cada campo com as validacoes estaticas e diz qual esta errado
    private void validar(MaquinaCadastroRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do equipamento sao obrigatorios.");
        }
        if (!MaquinaCadastroRequest.isTipoEquipamentoValido(request.getTipoEquipamento())) {
            throw new IllegalArgumentException("Tipo de equipamento invalido.");
        }
        if (!MaquinaCadastroRequest.isTituloValido(request.getTituloAnuncio())) {
            throw new IllegalArgumentException("O titulo do anuncio deve ter de "
                    + MaquinaCadastroRequest.TITULO_MINIMO + " a " + MaquinaCadastroRequest.TITULO_MAXIMO + " caracteres.");
        }
        if (!MaquinaCadastroRequest.isMunicipioValido(request.getMunicipio())) {
            throw new IllegalArgumentException("Municipio invalido.");
        }
        if (!MaquinaCadastroRequest.isValorDiariaValido(request.getValorDiaria())) {
            throw new IllegalArgumentException("O valor da diaria deve ser maior que zero e de no maximo R$ 50.000,00.");
        }
    }
}
