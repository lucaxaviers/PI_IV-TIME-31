/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Servico de busca geoespacial 2dsphere por raio e consulta de maquina por id (Issue #36)
*/

package com.alugamaquina.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.stereotype.Service;

import com.alugamaquina.backend.dto.MaquinaBuscaResponse;
import com.alugamaquina.backend.dto.MaquinaDetalheResponse;
import com.alugamaquina.backend.exception.MaquinaNaoEncontradaException;
import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.repository.MaquinaRepository;

@Service
public class MaquinaBuscaService {

    // Limite maximo aceito pelo endpoint para evitar varreduras globais desnecessarias
    private static final double RAIO_MAXIMO_KM = 500;

    private final MongoTemplate mongoTemplate;
    private final MaquinaRepository maquinaRepository;

    public MaquinaBuscaService(MongoTemplate mongoTemplate, MaquinaRepository maquinaRepository) {
        this.mongoTemplate = mongoTemplate;
        this.maquinaRepository = maquinaRepository;
    }

    // Executa consulta geoespacial esferica (geoNear) ordenando da maquina mais proxima para a mais distante
    public List<MaquinaBuscaResponse> buscarPorRaio(double lat, double lng, double raioKm) {
        validarParametros(lat, lng, raioKm);

        // No GeoJSON do MongoDB a coordenada segue sempre a ordem [longitude, latitude]
        GeoJsonPoint ponto = new GeoJsonPoint(lng, lat);
        NearQuery consulta = NearQuery.near(ponto)
                .maxDistance(new Distance(raioKm, Metrics.KILOMETERS))
                .spherical(true);

        GeoResults<Maquina> resultados = mongoTemplate.geoNear(consulta, Maquina.class);

        return resultados.getContent().stream()
                .map(resultado -> new MaquinaBuscaResponse(
                        resultado.getContent().getId(),
                        resultado.getContent().getTituloAnuncio(),
                        resultado.getContent().getTipoEquipamento(),
                        resultado.getContent().getMunicipio(),
                        resultado.getContent().getValorDiaria(),
                        resultado.getDistance().getValue()))
                .collect(Collectors.toList());
    }

    // Busca uma maquina pelo id para a tela de detalhe. 404 se nao existir ou o id for vazio.
    public MaquinaDetalheResponse buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new MaquinaNaoEncontradaException("Id da maquina e obrigatorio.");
        }

        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new MaquinaNaoEncontradaException("Maquina nao encontrada: " + id));

        return new MaquinaDetalheResponse(
                maquina.getId(),
                maquina.getTituloAnuncio(),
                maquina.getTipoEquipamento(),
                maquina.getMunicipio(),
                maquina.getValorDiaria(),
                // getY() = latitude e getX() = longitude no GeoJsonPoint
                maquina.getLocalizacao().getY(),
                maquina.getLocalizacao().getX());
    }

    // Garante coordenadas geograficas validas e raio dentro do limite permitido (0 < raioKm <= 500)
    private void validarParametros(double lat, double lng, double raioKm) {
        if (lat < -90 || lat > 90) {
            throw new ParametroBuscaInvalidoException("Latitude deve estar entre -90 e 90.");
        }
        if (lng < -180 || lng > 180) {
            throw new ParametroBuscaInvalidoException("Longitude deve estar entre -180 e 180.");
        }
        if (raioKm <= 0 || raioKm > RAIO_MAXIMO_KM) {
            throw new ParametroBuscaInvalidoException("Raio deve ser maior que 0 e no maximo " + RAIO_MAXIMO_KM + " km.");
        }
    }
}
