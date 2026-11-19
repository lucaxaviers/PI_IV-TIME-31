/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Testes do gerador de PDF do contrato digital (particionamento por estado das assinaturas)
*/

package com.alugamaquina.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alugamaquina.backend.model.Contrato;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

/**
 * Requisito de teste RT-02 (Issue #44): o PDF do contrato precisa ser um PDF valido com os dados da locacao.
 *
 * Particionamento de Equivalencia (entrada: o contrato)
 *   V1 contrato sem assinaturas | V2 so o locador assinou | V3 so o locatario assinou | V4 as duas partes assinaram
 *   I1 contrato nulo
 * BVA: periodo de 1 diaria (limite) e de varias diarias; caucao zero (limite).
 */
class GeradorPdfContratoTest {

    private GeradorPdfContrato gerador;

    @BeforeEach
    void preparar() {
        gerador = new GeradorPdfContrato();
    }

    private Contrato criarContrato(LocalDate inicio, LocalDate fim, String caucao) throws Exception {
        return new Contrato("loc1", "AM-2026-00123", "locador1", "locatario1", "maq1",
                "Carlos Eduardo Pereira", "Antonio Ferreira da Silva", "Trator John Deere 6115J",
                inicio, fim, new BigDecimal("4614.00"), new BigDecimal(caucao));
    }

    private Contrato criarContrato() throws Exception {
        return criarContrato(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14), "1000.00");
    }

    // Le o PDF de volta e junta o texto de todas as paginas
    private String lerTexto(byte[] pdf) throws IOException {
        PdfReader leitor = new PdfReader(pdf);
        PdfTextExtractor extrator = new PdfTextExtractor(leitor);
        StringBuilder texto = new StringBuilder();
        for (int pagina = 1; pagina <= leitor.getNumberOfPages(); pagina++) {
            texto.append(extrator.getTextFromPage(pagina)).append('\n');
        }
        leitor.close();
        return texto.toString();
    }

    // CT-PDF-01 | V1: o arquivo gerado e um PDF
    @Test
    void testPdfGeradoComecaComCabecalhoDePdf() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();

        // Action
        byte[] pdf = gerador.gerar(contrato);

        // Assert
        assertTrue(pdf.length > 500);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    // CT-PDF-02 | V1: o PDF traz numero, partes e equipamento
    @Test
    void testPdfTrazNumeroPartesEEquipamento() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("AM-2026-00123"));
        assertTrue(texto.contains("Carlos Eduardo Pereira"));
        assertTrue(texto.contains("Antonio Ferreira da Silva"));
        assertTrue(texto.contains("Trator John Deere 6115J"));
    }

    // CT-PDF-03 | V1: o PDF traz as 6 clausulas
    @Test
    void testPdfTrazAsSeisClausulas() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        for (int numero = 1; numero <= 6; numero++) {
            assertTrue(texto.contains("Cláusula " + numero + "ª"), "falta a clausula " + numero);
        }
    }

    // CT-PDF-04 | V1: valores e datas formatados em reais e dd/MM/yyyy
    @Test
    void testPdfTrazValoresEPeriodoFormatados() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("R$ 4.614,00"));
        assertTrue(texto.contains("R$ 1.000,00"));
        assertTrue(texto.contains("10/11/2026 a 14/11/2026"));
        assertTrue(texto.contains("5 diárias"));
    }

    // CT-PDF-05 | BVA: limite de 1 diaria usa o singular
    @Test
    void testPdfComUmaDiariaUsaOSingular() throws Exception {
        // Arrange
        LocalDate dia = LocalDate.of(2026, 11, 10);
        Contrato contrato = criarContrato(dia, dia, "1000.00");

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("1 diária"));
        assertTrue(!texto.contains("1 diárias"));
    }

    // CT-PDF-06 | BVA: caucao zero e escrita como R$ 0,00
    @Test
    void testPdfComCaucaoZeroMostraZeroReais() throws Exception {
        // Arrange
        Contrato contrato = criarContrato(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14), "0");

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("R$ 0,00"));
    }

    // CT-PDF-07 | V1: sem assinaturas o PDF tem as linhas em branco
    @Test
    void testPdfSemAssinaturasTemLinhasEmBranco() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("______"));
        assertTrue(!texto.contains("Assinado digitalmente"));
    }

    // CT-PDF-08 | V2: so o locador assinou
    @Test
    void testPdfComSoOLocadorAssinadoMostraSoAAssinaturaDele() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();
        contrato.assineComoLocador();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("Assinado digitalmente por Carlos Eduardo Pereira"));
        assertTrue(!texto.contains("Assinado digitalmente por Antonio Ferreira da Silva"));
    }

    // CT-PDF-09 | V3: so o locatario assinou
    @Test
    void testPdfComSoOLocatarioAssinadoMostraSoAAssinaturaDele() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();
        contrato.assineComoLocatario();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("Assinado digitalmente por Antonio Ferreira da Silva"));
        assertTrue(!texto.contains("Assinado digitalmente por Carlos Eduardo Pereira"));
    }

    // CT-PDF-10 | V4: as duas partes assinaram
    @Test
    void testPdfComAsDuasAssinaturasMostraAsDuas() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();
        contrato.assineComoLocador();
        contrato.assineComoLocatario();

        // Action
        String texto = lerTexto(gerador.gerar(contrato));

        // Assert
        assertTrue(texto.contains("Assinado digitalmente por Carlos Eduardo Pereira"));
        assertTrue(texto.contains("Assinado digitalmente por Antonio Ferreira da Silva"));
        assertTrue(!texto.contains("______"));
    }

    // CT-PDF-11 | I1: contrato nulo
    @Test
    void testPdfDeContratoNuloDaErro() {
        // Arrange
        Contrato contrato = null;

        // Action
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> gerador.gerar(contrato));

        // Assert
        assertEquals("Contrato e obrigatorio.", erro.getMessage());
    }

    // CT-PDF-12 | nome do arquivo baixado
    @Test
    void testNomeDoArquivoUsaONumeroDoContrato() throws Exception {
        // Arrange
        Contrato contrato = criarContrato();

        // Action
        String nome = GeradorPdfContrato.nomeDoArquivo(contrato);

        // Assert
        assertEquals("contrato-AM-2026-00123.pdf", nome);
    }
}
