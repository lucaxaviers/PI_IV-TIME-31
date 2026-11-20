package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import com.alugamaquina.backend.dto.MaquinaDetalheResponse;
import com.alugamaquina.backend.exception.MaquinaNaoEncontradaException;
import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.repository.MaquinaRepository;

class MaquinaBuscaServiceTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final MaquinaRepository maquinaRepository = mock(MaquinaRepository.class);
    private final MaquinaBuscaService service = new MaquinaBuscaService(mongoTemplate, maquinaRepository);

    @Test
    void latitudeForaDoIntervaloLancaExcecao() {
        assertThrows(ParametroBuscaInvalidoException.class,
                () -> service.buscarPorRaio(200, -46.6, 10));
    }

    @Test
    void longitudeForaDoIntervaloLancaExcecao() {
        assertThrows(ParametroBuscaInvalidoException.class,
                () -> service.buscarPorRaio(-23.5, 200, 10));
    }

    @Test
    void raioZeroOuNegativoLancaExcecao() {
        assertThrows(ParametroBuscaInvalidoException.class,
                () -> service.buscarPorRaio(-23.5, -46.6, 0));
    }

    @Test
    void raioAcimaDoLimiteLancaExcecao() {
        assertThrows(ParametroBuscaInvalidoException.class,
                () -> service.buscarPorRaio(-23.5, -46.6, 501));
    }

    // ---------- buscarPorId (Issue #37: GET /api/maquinas/{id}) ----------

    // CT-MBID-01 | id existente devolve os dados da maquina
    @Test
    void testBuscarPorIdComIdExistenteDevolveOsDadosDaMaquina() {
        // Arrange
        Maquina maquina = new Maquina("Trator Perto", "tratores", "Paulinia",
                new BigDecimal("500"), new GeoJsonPoint(-47.15, -22.76));
        maquina.setId("10");
        when(maquinaRepository.findById("10")).thenReturn(Optional.of(maquina));

        // Act
        MaquinaDetalheResponse resposta = service.buscarPorId("10");

        // Assert
        assertEquals("10", resposta.getId());
        assertEquals("Trator Perto", resposta.getTituloAnuncio());
        assertEquals("tratores", resposta.getTipoEquipamento());
        assertEquals("Paulinia", resposta.getMunicipio());
        assertEquals(new BigDecimal("500"), resposta.getValorDiaria());
        assertEquals(-22.76, resposta.getLatitude());
        assertEquals(-47.15, resposta.getLongitude());
    }

    // CT-MBID-02 | id inexistente lanca MaquinaNaoEncontradaException
    @Test
    void testBuscarPorIdComIdInexistenteLancaExcecao() {
        // Arrange
        when(maquinaRepository.findById("999")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(MaquinaNaoEncontradaException.class, () -> service.buscarPorId("999"));
    }

    // CT-MBID-03 | id nulo lanca MaquinaNaoEncontradaException sem consultar o banco
    @Test
    void testBuscarPorIdComIdNuloLancaExcecao() {
        assertThrows(MaquinaNaoEncontradaException.class, () -> service.buscarPorId(null));
    }

    // CT-MBID-04 | id vazio ou so com espacos lanca MaquinaNaoEncontradaException
    @Test
    void testBuscarPorIdComIdVazioLancaExcecao() {
        assertThrows(MaquinaNaoEncontradaException.class, () -> service.buscarPorId("   "));
    }
}
