/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Testes OO Interclasse (integracao e ponta a ponta) do fluxo de cadastro de maquinas
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.alugamaquina.backend.controller.MaquinaCadastroController;
import com.alugamaquina.backend.dto.MaquinaCadastroRequest;
import com.alugamaquina.backend.dto.MaquinaCadastroResponse;
import com.alugamaquina.backend.model.Maquina;
import com.alugamaquina.backend.repository.MaquinaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes de integracao entre classes, no padrao AAA.
 *
 * Estrategia incremental (duas classes por vez):
 *   MaquinaCadastroController -> MaquinaCadastroService   (resposta e erro viram HTTP)
 *   MaquinaCadastroService -> MaquinaCadastroRequest -> enums (validacoes estaticas e conversao)
 *
 * Estrategia ponta a ponta (thread "o formulario envia o JSON e a maquina e gravada"):
 *   JSON -> MaquinaCadastroRequest -> Controller -> Service -> enums -> Maquina -> MaquinaRepository (simulado)
 */
class MaquinaCadastroInterclasseTest {

    private final ObjectMapper leitorJson = new ObjectMapper();
    private List<Maquina> bancoDeMaquinas;
    private MaquinaCadastroController controller;

    @BeforeEach
    void preparar() {
        bancoDeMaquinas = new ArrayList<>();
        MaquinaRepository repositorio = mock(MaquinaRepository.class);

        // Repositorio em memoria: guarda a maquina e gera um id sequencial
        when(repositorio.save(any(Maquina.class))).thenAnswer(chamada -> {
            Maquina maquina = chamada.getArgument(0);
            maquina.setId("maq-" + (bancoDeMaquinas.size() + 100));
            bancoDeMaquinas.add(maquina);
            return maquina;
        });

        controller = new MaquinaCadastroController(new MaquinaCadastroService(repositorio));
    }

    // Le o JSON como o Spring faz: construtor vazio e setters
    private MaquinaCadastroRequest lerJson(String json) throws Exception {
        return leitorJson.readValue(json, MaquinaCadastroRequest.class);
    }

    // ---------- Integracao incremental: Controller -> Servico ----------

    // CT-CINT-01 | cadastrar responde 201 com o id da maquina gravada
    @Test
    void testControllerCadastrarResponde201ComOId() throws Exception {
        // Arrange
        MaquinaCadastroRequest pedido = lerJson(
                "{\"tituloAnuncio\":\"Trator John Deere 6115J\",\"tipoEquipamento\":\"trator\",\"municipio\":\"paulinia\",\"valorDiaria\":850.00}");

        // Action
        ResponseEntity<MaquinaCadastroResponse> resposta = controller.cadastrar(pedido);

        // Assert
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody().getId());
        assertTrue(resposta.getBody().isSucesso());
    }

    // CT-CINT-02 | dado invalido sobe como erro e o tratador do controller devolve 400 com a mensagem
    @Test
    void testControllerTraduzDadoInvalidoParaHttp400() throws Exception {
        // Arrange
        MaquinaCadastroRequest pedido = lerJson(
                "{\"tituloAnuncio\":\"abc\",\"tipoEquipamento\":\"trator\",\"municipio\":\"paulinia\",\"valorDiaria\":850}");

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> controller.cadastrar(pedido));
        ResponseEntity<Map<String, Object>> resposta = controller.tratarDadoInvalido(erro);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(false, resposta.getBody().get("sucesso"));
        assertEquals("O titulo do anuncio deve ter de 5 a 100 caracteres.", resposta.getBody().get("erro"));
    }

    // ---------- Integracao incremental: Servico -> Pedido e enums ----------

    // CT-CINT-03 | o servico usa as validacoes estaticas do pedido para recusar campo invalido
    @Test
    void testServicoRecusaOQueAsValidacoesEstaticasRecusam() throws Exception {
        // Arrange
        MaquinaCadastroRequest pedido = lerJson(
                "{\"tituloAnuncio\":\"Trator John Deere\",\"tipoEquipamento\":\"trator\",\"municipio\":\"paulinia\",\"valorDiaria\":50000.01}");

        // Action
        boolean pedidoValido = pedido.isValido();
        assertThrows(IllegalArgumentException.class, () -> controller.cadastrar(pedido));

        // Assert
        assertFalse(pedidoValido);
        assertTrue(bancoDeMaquinas.isEmpty());
    }

    // ---------- Ponta a ponta ----------

    // CT-CE2E-01 | cenario normal: JSON do formulario vira maquina gravada no formato do catalogo
    @Test
    void testCenarioNormalJsonDoFormularioViraMaquinaGravada() throws Exception {
        // Arrange - mesmo JSON que o cadastro-maquina.js envia
        String json = "{\"tituloAnuncio\":\"Colheitadeira Case 2022\",\"tipoEquipamento\":\"colheitadeira\","
                + "\"municipio\":\"cosmopolis\",\"valorDiaria\":2400.50}";

        // Action
        ResponseEntity<MaquinaCadastroResponse> resposta = controller.cadastrar(lerJson(json));

        // Assert
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals(1, bancoDeMaquinas.size());
        Maquina gravada = bancoDeMaquinas.get(0);
        assertEquals("Colheitadeira Case 2022", gravada.getTituloAnuncio());
        assertEquals("colheitadeiras", gravada.getTipoEquipamento());
        assertEquals("Cosmópolis / SP", gravada.getMunicipio());
        assertEquals("2400.50", gravada.getValorDiaria().toPlainString());
        assertEquals(-47.1960, gravada.getLocalizacao().getX());
        assertEquals(-22.6458, gravada.getLocalizacao().getY());
    }

    // CT-CE2E-02 | cenario normal: duas maquinas cadastradas ficam com ids diferentes
    @Test
    void testDuasMaquinasCadastradasGanhamIdsDiferentes() throws Exception {
        // Arrange
        String json = "{\"tituloAnuncio\":\"Trator Valtra BH180\",\"tipoEquipamento\":\"trator\",\"municipio\":\"campinas\",\"valorDiaria\":700}";

        // Action
        String primeiro = controller.cadastrar(lerJson(json)).getBody().getId();
        String segundo = controller.cadastrar(lerJson(json)).getBody().getId();

        // Assert
        assertEquals(2, bancoDeMaquinas.size());
        assertFalse(primeiro.equals(segundo));
    }

    // CT-CE2E-03 | excecao: JSON sem campos vira pedido vazio e nada e gravado
    @Test
    void testCenarioDeExcecaoJsonVazioNaoGravaNada() throws Exception {
        // Arrange
        MaquinaCadastroRequest pedidoVazio = lerJson("{}");

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> controller.cadastrar(pedidoVazio));

        // Assert
        assertEquals("Tipo de equipamento invalido.", erro.getMessage());
        assertTrue(bancoDeMaquinas.isEmpty());
    }

    // CT-CE2E-04 | excecao: corpo ausente (nulo) tambem e recusado sem gravar
    @Test
    void testCenarioDeExcecaoCorpoAusenteNaoGravaNada() {
        // Arrange - corpo nulo

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> controller.cadastrar(null));

        // Assert
        assertEquals("Dados do equipamento sao obrigatorios.", erro.getMessage());
        assertTrue(bancoDeMaquinas.isEmpty());
    }

    // CT-CE2E-05 | excecao: um campo errado entre varios validos nao grava a maquina
    @Test
    void testCenarioDeExcecaoMunicipioErradoNaoGravaMesmoComOsDemaisValidos() throws Exception {
        // Arrange
        String json = "{\"tituloAnuncio\":\"Pulverizador Jacto 2020\",\"tipoEquipamento\":\"pulverizador\","
                + "\"municipio\":\"sao-paulo\",\"valorDiaria\":1300}";

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> controller.cadastrar(lerJson(json)));

        // Assert
        assertEquals("Municipio invalido.", erro.getMessage());
        assertTrue(bancoDeMaquinas.isEmpty());
    }
}
