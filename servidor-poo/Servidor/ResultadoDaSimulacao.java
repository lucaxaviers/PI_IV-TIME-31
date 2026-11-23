/*
  Autor: Tiago Medeiros | 25000845
  Descricao: Comunicado devolvido pelo Servidor com os valores calculados da simulacao de locacao
*/

/**
 * Resposta estruturada do servidor contendo o detalhamento de custos da simulacao solicitada.
 */
public class ResultadoDaSimulacao extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private boolean sucesso;
    private String  mensagem;
    private String  descricaoMaquina;
    private int     quantidadeDias;
    private double  distanciaKm;
    private double  subtotalDiarias;
    private double  valorFrete;
    private double  valorCaucao;
    private double  valorTotal;

    public ResultadoDaSimulacao (boolean sucesso,
                                 String mensagem,
                                 String descricaoMaquina,
                                 int quantidadeDias,
                                 double distanciaKm,
                                 double subtotalDiarias,
                                 double valorFrete,
                                 double valorCaucao,
                                 double valorTotal)
    {
        this.sucesso          = sucesso;
        this.mensagem         = mensagem;
        this.descricaoMaquina = descricaoMaquina;
        this.quantidadeDias   = quantidadeDias;
        this.distanciaKm      = distanciaKm;
        this.subtotalDiarias  = subtotalDiarias;
        this.valorFrete       = valorFrete;
        this.valorCaucao      = valorCaucao;
        this.valorTotal       = valorTotal;
    }

    public boolean isSucesso ()
    {
        return this.sucesso;
    }

    public String getMensagem ()
    {
        return this.mensagem;
    }

    public String getDescricaoMaquina ()
    {
        return this.descricaoMaquina;
    }

    public int getQuantidadeDias ()
    {
        return this.quantidadeDias;
    }

    public double getDistanciaKm ()
    {
        return this.distanciaKm;
    }

    public double getSubtotalDiarias ()
    {
        return this.subtotalDiarias;
    }

    public double getValorFrete ()
    {
        return this.valorFrete;
    }

    public double getValorCaucao ()
    {
        return this.valorCaucao;
    }

    public double getValorTotal ()
    {
        return this.valorTotal;
    }

    // Formata o resumo completo da simulacao para exibicao direta no console do Cliente
    @Override
    public String toString ()
    {
        if (!this.sucesso)
            return "Simulacao recusada: " + this.mensagem;

        return String.format(
            "Maquina: %s | Dias: %d | Distancia: %.1f km%n"
          + "Subtotal diarias: R$ %.2f%n"
          + "Frete (ida e volta + mobilizacao): R$ %.2f%n"
          + "Caucao padrao: R$ %.2f%n"
          + "Total estimado (diarias + frete): R$ %.2f",
            this.descricaoMaquina, this.quantidadeDias, this.distanciaKm,
            this.subtotalDiarias, this.valorFrete, this.valorCaucao, this.valorTotal
        );
    }
}
