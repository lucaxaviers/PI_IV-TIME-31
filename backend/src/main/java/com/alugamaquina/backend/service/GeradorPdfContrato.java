/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Gera o PDF do contrato digital (clausulas e assinaturas) com a biblioteca OpenPDF
*/

package com.alugamaquina.backend.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.alugamaquina.backend.model.Contrato;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Component
public class GeradorPdfContrato {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Fontes do documento
    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    private static final Font FONTE_CODIGO = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONTE_CLAUSULA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10.5f);

    /**
     * Monta o PDF do contrato e devolve os bytes do arquivo.
     */
    public byte[] gerar(Contrato contrato) {
        if (contrato == null) {
            throw new IllegalArgumentException("Contrato e obrigatorio.");
        }

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 56, 56, 56, 56);

        try {
            PdfWriter.getInstance(documento, saida);
            documento.open();

            adicionarCabecalho(documento, contrato);
            adicionarClausulas(documento, contrato);
            adicionarAssinaturas(documento, contrato);

            documento.close();
        } catch (DocumentException erro) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF do contrato.", erro);
        }

        return saida.toByteArray();
    }

    // Nome do arquivo que o navegador vai baixar
    public static String nomeDoArquivo(Contrato contrato) {
        return "contrato-" + contrato.getNumero() + ".pdf";
    }

    private void adicionarCabecalho(Document documento, Contrato contrato) throws DocumentException {
        Paragraph titulo = new Paragraph("CONTRATO DE LOCAÇÃO DE EQUIPAMENTO AGRÍCOLA", FONTE_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph codigo = new Paragraph("Nº " + contrato.getNumero() + " · Emitido via AlugaMáquina", FONTE_CODIGO);
        codigo.setAlignment(Element.ALIGN_CENTER);
        codigo.setSpacingAfter(18);
        documento.add(codigo);

        documento.add(new Paragraph("LOCADOR: " + contrato.getNomeLocador(), FONTE_TEXTO));
        Paragraph locatario = new Paragraph("LOCATÁRIO: " + contrato.getNomeLocatario(), FONTE_TEXTO);
        locatario.setSpacingAfter(14);
        documento.add(locatario);
    }

    // As 6 clausulas da minuta, preenchidas com os dados do contrato
    private void adicionarClausulas(Document documento, Contrato contrato) throws DocumentException {
        long diarias = ChronoUnit.DAYS.between(contrato.getDataInicio(), contrato.getDataFim()) + 1;
        String periodo = contrato.getDataInicio().format(FORMATO_DATA) + " a " + contrato.getDataFim().format(FORMATO_DATA);

        adicionarClausula(documento, "Cláusula 1ª - Do objeto",
                "O LOCADOR cede ao LOCATÁRIO, em caráter temporário, o equipamento "
                        + contrato.getDescricaoMaquina() + ", em perfeito estado de funcionamento.");

        adicionarClausula(documento, "Cláusula 2ª - Do prazo",
                "A locação vigora de " + periodo + " (" + diarias + (diarias == 1 ? " diária" : " diárias")
                        + "), devendo o equipamento ser devolvido no último dia, no mesmo local da retirada.");

        adicionarClausula(documento, "Cláusula 3ª - Do valor e do frete",
                "O valor total é de " + formatarReais(contrato.getValorTotal())
                        + ", já incluindo as diárias e o frete estimado, pago conforme combinado na plataforma.");

        adicionarClausula(documento, "Cláusula 4ª - Dos termos de uso",
                "O LOCATÁRIO compromete-se a utilizar o equipamento apenas para fins agrícolas, por operador "
                        + "habilitado, seguindo as orientações do fabricante e as regras de uso da plataforma.");

        adicionarClausula(documento, "Cláusula 5ª - Dos danos e da caução",
                "Foi retida caução de " + formatarReais(contrato.getValorCaucao()) + ". Em caso de danos não "
                        + "decorrentes do desgaste normal, apurados no checklist de devolução, o valor do reparo "
                        + "será abatido da caução, cabendo ao LOCATÁRIO complementar eventual diferença.");

        adicionarClausula(documento, "Cláusula 6ª - Do foro",
                "As partes elegem o foro da comarca do LOCADOR para dirimir quaisquer dúvidas deste contrato.");
    }

    private void adicionarClausula(Document documento, String titulo, String texto) throws DocumentException {
        Paragraph tituloClausula = new Paragraph(titulo, FONTE_CLAUSULA);
        tituloClausula.setSpacingBefore(8);
        documento.add(tituloClausula);

        Paragraph corpo = new Paragraph(texto, FONTE_TEXTO);
        corpo.setAlignment(Element.ALIGN_JUSTIFIED);
        documento.add(corpo);
    }

    // Quem ja assinou aparece com data e hora; quem nao assinou fica com a linha em branco
    private void adicionarAssinaturas(Document documento, Contrato contrato) throws DocumentException {
        Paragraph espaco = new Paragraph(" ");
        espaco.setSpacingBefore(24);
        documento.add(espaco);

        String locador = contrato.isAssinadoPeloLocador()
                ? "Assinado digitalmente por " + contrato.getNomeLocador() + " em "
                        + contrato.getDataAssinaturaLocador().format(FORMATO_DATA_HORA)
                : "______________________________";
        String locatario = contrato.isAssinadoPeloLocatario()
                ? "Assinado digitalmente por " + contrato.getNomeLocatario() + " em "
                        + contrato.getDataAssinaturaLocatario().format(FORMATO_DATA_HORA)
                : "______________________________";

        documento.add(new Paragraph(locador, FONTE_TEXTO));
        Paragraph papelLocador = new Paragraph("Locador", FONTE_CODIGO);
        papelLocador.setSpacingAfter(16);
        documento.add(papelLocador);

        documento.add(new Paragraph(locatario, FONTE_TEXTO));
        documento.add(new Paragraph("Locatário", FONTE_CODIGO));
    }

    // Formata em reais: R$ 4.614,00
    private String formatarReais(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor).replace(' ', ' ');
    }
}
