/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Testes JUnit 5 de MaquinaBuscaService para os resultados da busca geoespacial
             (lista vazia, ordem por distancia e conversao para MaquinaBuscaResponse), no
             padrao AAA (Issue #46)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.NearQuery;

import com.alugamaquina.backend.dto.MaquinaBuscaResponse;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.repository.MaquinaRepository;

class MaquinaBuscaServiceBuscaTest {

    private MongoTemplate mongoTemplate;
    private MaquinaBuscaService service;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        service = new MaquinaBuscaService(mongoTemplate, mock(MaquinaRepository.class));
    }

    private Maquina criarMaquina(String id, String titulo, String tipo, String municipio, String valor) {
        Maquina maquina = new Maquina(titulo, tipo, municipio, new BigDecimal(valor), new GeoJsonPoint(-47.1, -22.9));
        maquina.setId(id);
        return maquina;
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaMaquinaNoRaio() {
        // Arrange
        when(mongoTemplate.geoNear(any(NearQuery.class), eq(Maquina.class)))
                .thenReturn(new GeoResults<Maquina>(List.of()));

        // Act
        List<MaquinaBuscaResponse> resultado = service.buscarPorRaio(-23.5, -46.6, 50);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void devePreservarAOrdemPorDistanciaRetornadaPeloMongo() {
        // Arrange: Mongo ja devolve ordenado do mais proximo para o mais distante
        Maquina perto = criarMaquina("1", "Trator Perto", "trator", "Paulinia", "500");
        Maquina longe = criarMaquina("2", "Trator Longe", "trator", "Campinas", "600");

        GeoResult<Maquina> resultadoPerto = new GeoResult<>(perto, new Distance(2.0, Metrics.KILOMETERS));
        GeoResult<Maquina> resultadoLonge = new GeoResult<>(longe, new Distance(40.0, Metrics.KILOMETERS));

        when(mongoTemplate.geoNear(any(NearQuery.class), eq(Maquina.class)))
                .thenReturn(new GeoResults<>(List.of(resultadoPerto, resultadoLonge)));

        // Act
        List<MaquinaBuscaResponse> resultado = service.buscarPorRaio(-23.5, -46.6, 50);

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Trator Perto", resultado.get(0).getTituloAnuncio());
        assertEquals("Trator Longe", resultado.get(1).getTituloAnuncio());
        assertTrue(resultado.get(0).getDistanciaKm() < resultado.get(1).getDistanciaKm());
    }

    @Test
    void deveConverterCamposDaMaquinaParaOResponseCorretamente() {
        // Arrange
        Maquina maquina = criarMaquina("10", "Colheitadeira Azul", "colheitadeira", "Valinhos", "1900");
        GeoResult<Maquina> resultadoGeo = new GeoResult<>(maquina, new Distance(12.5, Metrics.KILOMETERS));

        when(mongoTemplate.geoNear(any(NearQuery.class), eq(Maquina.class)))
                .thenReturn(new GeoResults<>(List.of(resultadoGeo)));

        // Act
        List<MaquinaBuscaResponse> resultado = service.buscarPorRaio(-23.5, -46.6, 50);

        // Assert
        MaquinaBuscaResponse response = resultado.get(0);
        assertEquals("10", response.getId());
        assertEquals("Colheitadeira Azul", response.getTituloAnuncio());
        assertEquals("colheitadeira", response.getTipoEquipamento());
        assertEquals("Valinhos", response.getMunicipio());
        assertEquals(new BigDecimal("1900"), response.getValorDiaria());
        assertEquals(12.5, response.getDistanciaKm());
    }
}
