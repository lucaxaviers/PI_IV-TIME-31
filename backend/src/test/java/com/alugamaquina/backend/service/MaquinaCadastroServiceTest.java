/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes unitarios do MaquinaCadastroService com repositorio simulado (Equivalencia e BVA)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.alugamaquina.backend.dto.MaquinaCadastroRequest;
import com.alugamaquina.backend.dto.MaquinaCadastroResponse;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.model.MunicipioAtendido;
import com.alugamaquina.backend.model.TipoEquipamento;
import com.alugamaquina.backend.repository.MaquinaRepository;

/**
 * Requisito de teste RT-09: cadastrar so grava a maquina com dados validos, e grava no formato que o catalogo usa.
 *
 * Particionamento de Equivalencia
 *   cadastrar(pedido)  V1 pedido com os quatro campos validos
 *                      I1 pedido nulo | I2 tipo invalido | I3 titulo invalido | I4 municipio invalido | I5 valor invalido
 *
 * BVA: titulo com 4 e 5 caracteres; valor da diaria em 0,00 / 0,01 e 50.000,00 / 50.000,01.
 *
 * O repositorio e simulado (mock), porque o objeto do teste e so a regra do servico.
 */
class MaquinaCadastroServiceTest {

    private MaquinaRepository maquinaRepository;
    private MaquinaCadastroService servico;

    @BeforeEach
    void preparar() {
        maquinaRepository = mock(MaquinaRepository.class);
        servico = new MaquinaCadastroService(maquinaRepository);

        // O save devolve o objeto recebido, ja com o id que o MongoDB geraria
        when(maquinaRepository.save(any(Maquina.class))).thenAnswer(chamada -> {
            Maquina maquina = chamada.getArgument(0);
            maquina.setId("maq-novo-01");
            return maquina;
        });
    }

    // ---------- Massa de dados ----------

    private MaquinaCadastroRequest criarPedido(String titulo, String tipo, String municipio, String valor) {
        MaquinaCadastroRequest pedido = new MaquinaCadastroRequest();
        pedido.setTituloAnuncio(titulo);
        pedido.setTipoEquipamento(tipo);
        pedido.setMunicipio(municipio);
        pedido.setValorDiaria(valor == null ? null : new BigDecimal(valor));
        return pedido;
    }

    private MaquinaCadastroRequest criarPedidoValido() {
        return criarPedido("  Trator John Deere 6115J  ", "trator", "paulinia", "850");
    }

    private Maquina capturarMaquinaSalva() {
        ArgumentCaptor<Maquina> captor = ArgumentCaptor.forClass(Maquina.class);
        verify(maquinaRepository).save(captor.capture());
        return captor.getValue();
    }

    // ---------- Classe valida ----------

    // CT-CAD-01 | V1: a resposta traz o id gerado e os dados gravados
    @Test
    void testCadastrarDevolveOIdGeradoEOsDadosGravados() {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedidoValido();

        // Action
        MaquinaCadastroResponse resposta = servico.cadastrar(pedido);

        // Assert
        assertTrue(resposta.isSucesso());
        assertEquals("maq-novo-01", resposta.getId());
        assertEquals("Trator John Deere 6115J", resposta.getTituloAnuncio());
        assertEquals("tratores", resposta.getTipoEquipamento());
        assertEquals("Paulínia / SP", resposta.getMunicipio());
        assertEquals(new BigDecimal("850.00"), resposta.getValorDiaria());
    }

    // CT-CAD-02 | V1: grava a maquina no formato do catalogo (titulo aparado, tipo no plural, municipio com nome de exibicao)
    @Test
    void testCadastrarGravaNoFormatoDoCatalogo() {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedidoValido();

        // Action
        servico.cadastrar(pedido);

        // Assert
        Maquina gravada = capturarMaquinaSalva();
        assertEquals("Trator John Deere 6115J", gravada.getTituloAnuncio());
        assertEquals("tratores", gravada.getTipoEquipamento());
        assertEquals("Paulínia / SP", gravada.getMunicipio());
        assertEquals(new BigDecimal("850.00"), gravada.getValorDiaria());
    }

    // CT-CAD-03 | V1: a localizacao gravada usa longitude e depois latitude do municipio (ordem do GeoJSON)
    @Test
    void testCadastrarGravaALocalizacaoDoMunicipioEmOrdemGeoJson() {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Colheitadeira Case 2022", "colheitadeira", "cosmopolis", "2400");

        // Action
        servico.cadastrar(pedido);

        // Assert
        Maquina gravada = capturarMaquinaSalva();
        assertEquals(-47.1960, gravada.getLocalizacao().getX());     // longitude primeiro
        assertEquals(-22.6458, gravada.getLocalizacao().getY());     // latitude depois
    }

    // CT-CAD-04 | V1: cada municipio da lista grava a propria coordenada
    @ParameterizedTest(name = "CT-CAD-04 municipio={0}")
    @EnumSource(MunicipioAtendido.class)
    void testCadastrarGravaACoordenadaDeCadaMunicipio(MunicipioAtendido municipio) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Equipamento de teste", "trator", municipio.getCodigo(), "100");

        // Action
        servico.cadastrar(pedido);

        // Assert
        Maquina gravada = capturarMaquinaSalva();
        assertEquals(municipio.getNome(), gravada.getMunicipio());
        assertEquals(municipio.getLongitude(), gravada.getLocalizacao().getX());
        assertEquals(municipio.getLatitude(), gravada.getLocalizacao().getY());
    }

    // CT-CAD-05 | V1: cada tipo da lista grava a chave plural do catalogo
    @ParameterizedTest(name = "CT-CAD-05 tipo={0}")
    @EnumSource(TipoEquipamento.class)
    void testCadastrarGravaAChaveDoCatalogoDeCadaTipo(TipoEquipamento tipo) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Equipamento de teste", tipo.getCodigo(), "campinas", "100");

        // Action
        servico.cadastrar(pedido);

        // Assert
        assertEquals(tipo.getChaveCatalogo(), capturarMaquinaSalva().getTipoEquipamento());
    }

    // CT-CAD-06 | V1: codigos com maiusculas e espacos tambem funcionam
    @Test
    void testCadastrarAceitaCodigosComMaiusculasEEspacos() {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Plantadeira Jumil", " PLANTADEIRA ", " Sumare ", "900");

        // Action
        servico.cadastrar(pedido);

        // Assert
        Maquina gravada = capturarMaquinaSalva();
        assertEquals("plantadeiras", gravada.getTipoEquipamento());
        assertEquals("Sumaré / SP", gravada.getMunicipio());
    }

    // ---------- Classes invalidas ----------

    // CT-CAD-07 | I1: pedido nulo
    @Test
    void testCadastrarComPedidoNuloDaErro() {
        // Arrange - pedido nulo

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(null));

        // Assert
        assertEquals("Dados do equipamento sao obrigatorios.", erro.getMessage());
        verify(maquinaRepository, never()).save(any());
    }

    // CT-CAD-08 | I2: tipo nulo, vazio ou fora da lista
    @ParameterizedTest(name = "CT-CAD-08 tipo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"caminhao", "tratores"})
    void testCadastrarRejeitaTipoInvalido(String tipo) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Trator John Deere", tipo, "paulinia", "850");

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(pedido));

        // Assert
        assertEquals("Tipo de equipamento invalido.", erro.getMessage());
        verify(maquinaRepository, never()).save(any());
    }

    // CT-CAD-09 | I3: titulo nulo, vazio ou so espacos
    @ParameterizedTest(name = "CT-CAD-09 titulo=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"      "})
    void testCadastrarRejeitaTituloVazio(String titulo) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido(titulo, "trator", "paulinia", "850");

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(pedido));

        // Assert
        assertEquals("O titulo do anuncio deve ter de 5 a 100 caracteres.", erro.getMessage());
        verify(maquinaRepository, never()).save(any());
    }

    // CT-CAD-10 | I4: municipio nulo, vazio ou fora da lista
    @ParameterizedTest(name = "CT-CAD-10 municipio=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"sao-paulo", "Paulínia / SP"})
    void testCadastrarRejeitaMunicipioInvalido(String municipio) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Trator John Deere", "trator", municipio, "850");

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(pedido));

        // Assert
        assertEquals("Municipio invalido.", erro.getMessage());
        verify(maquinaRepository, never()).save(any());
    }

    // CT-CAD-11 | I5: valor nulo, zero ou negativo
    @ParameterizedTest(name = "CT-CAD-11 valor={0}")
    @CsvSource(value = {"NULL", "0", "-0.01", "-850"}, nullValues = "NULL")
    void testCadastrarRejeitaValorNuloZeroOuNegativo(String valor) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Trator John Deere", "trator", "paulinia", valor);

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(pedido));

        // Assert
        assertEquals("O valor da diaria deve ser maior que zero e de no maximo R$ 50.000,00.", erro.getMessage());
        verify(maquinaRepository, never()).save(any());
    }

    // ---------- BVA ----------

    // CT-BVA-30 | titulo: 4 caracteres (abaixo do limite) e 5 caracteres (limite)
    @ParameterizedTest(name = "CT-BVA-30 titulo com {0} caracteres -> aceito={1}")
    @CsvSource({"4, false", "5, true", "100, true", "101, false"})
    void testBvaDoTituloNoCadastro(int tamanho, boolean aceito) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("a".repeat(tamanho), "trator", "paulinia", "850");

        // Action + Assert
        if (aceito) {
            assertEquals("maq-novo-01", servico.cadastrar(pedido).getId());
        } else {
            assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(pedido));
        }
    }

    // CT-BVA-31 | valor: 0,00 (invalido), 0,01 (limite), 50.000,00 (limite) e 50.000,01 (acima)
    @ParameterizedTest(name = "CT-BVA-31 valor={0} -> aceito={1}")
    @CsvSource({"0.00, false", "0.01, true", "50000.00, true", "50000.01, false"})
    void testBvaDoValorNoCadastro(String valor, boolean aceito) {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Trator John Deere", "trator", "paulinia", valor);

        // Action + Assert
        if (aceito) {
            assertEquals(new BigDecimal(valor), servico.cadastrar(pedido).getValorDiaria());
        } else {
            assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(pedido));
        }
    }

    // CT-BVA-32 | valor com casas a mais e arredondado para 2 casas
    @Test
    void testValorComTresCasasEArredondadoParaDuasCasas() {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedido("Trator John Deere", "trator", "paulinia", "850.555");

        // Action
        servico.cadastrar(pedido);

        // Assert
        assertEquals(new BigDecimal("850.56"), capturarMaquinaSalva().getValorDiaria());
    }

    // CT-CAD-12 | o servico grava uma unica vez por cadastro
    @Test
    void testCadastrarGravaUmaUnicaVez() {
        // Arrange
        MaquinaCadastroRequest pedido = criarPedidoValido();

        // Action
        servico.cadastrar(pedido);

        // Assert
        verify(maquinaRepository).save(any(Maquina.class));
    }
}
