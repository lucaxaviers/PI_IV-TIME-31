package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alugamaquina.backend.exception.ParametroBuscaInvalidoException;

class MaquinaBuscaServiceTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final MaquinaBuscaService service = new MaquinaBuscaService(mongoTemplate);

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
}
