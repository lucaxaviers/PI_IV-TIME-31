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
import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;
import com.alugamaquina.backend.model.Maquina;

@Service
public class MaquinaBuscaService {

    private static final double RAIO_MAXIMO_KM = 500;

    private final MongoTemplate mongoTemplate;

    public MaquinaBuscaService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<MaquinaBuscaResponse> buscarPorRaio(double lat, double lng, double raioKm) {
        validarParametros(lat, lng, raioKm);

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
