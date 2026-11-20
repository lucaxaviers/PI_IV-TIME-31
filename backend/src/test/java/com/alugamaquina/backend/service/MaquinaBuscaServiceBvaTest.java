/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes JUnit 5 de MaquinaBuscaService com Analise de Valores de Fronteira (BVA),
             no padrao AAA (Issue #46)

  Fronteiras testadas:
    latitude   -90.01 (invalida) | -90 (valida) | 90 (valida) | 90.01 (invalida)
    longitude  -180.01 (invalida) | -180 (valida) | 180 (valida) | 180.01 (invalida)
    raio (km)  0 (invalido) | 0.01 (valido) | 500 (valido) | 500.01 (invalido)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.NearQuery;

import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.repository.MaquinaRepository;

class MaquinaBuscaServiceBvaTest {

    private MongoTemplate mongoTemplate;
    private MaquinaBuscaService service;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        service = new MaquinaBuscaService(mongoTemplate, mock(MaquinaRepository.class));
        when(mongoTemplate.geoNear(any(NearQuery.class), eq(Maquina.class)))
                .thenReturn(new GeoResults<Maquina>(List.of()));
    }

    // ---------- Latitude ----------

    @ParameterizedTest
    @ValueSource(doubles = {-90.01, 90.01, -200, 200})
    void latitudeForaDoIntervaloDeveLancarExcecao(double lat) {
        assertThrows(ParametroBuscaInvalidoException.class, () -> service.buscarPorRaio(lat, -46.6, 10));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-90, -89.99, 0, 89.99, 90})
    void latitudeNoIntervaloDeveSerAceita(double lat) {
        assertDoesNotThrow(() -> service.buscarPorRaio(lat, -46.6, 10));
    }

    // ---------- Longitude ----------

    @ParameterizedTest
    @ValueSource(doubles = {-180.01, 180.01, -300, 300})
    void longitudeForaDoIntervaloDeveLancarExcecao(double lng) {
        assertThrows(ParametroBuscaInvalidoException.class, () -> service.buscarPorRaio(-23.5, lng, 10));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-180, -179.99, 0, 179.99, 180})
    void longitudeNoIntervaloDeveSerAceita(double lng) {
        assertDoesNotThrow(() -> service.buscarPorRaio(-23.5, lng, 10));
    }

    // ---------- Raio ----------

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, -1, -500, 500.01, 501, 10000})
    void raioForaDoIntervaloDeveLancarExcecao(double raio) {
        assertThrows(ParametroBuscaInvalidoException.class, () -> service.buscarPorRaio(-23.5, -46.6, raio));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 1, 250, 499.99, 500})
    void raioNoIntervaloDeveSerAceito(double raio) {
        assertDoesNotThrow(() -> service.buscarPorRaio(-23.5, -46.6, raio));
    }
}
