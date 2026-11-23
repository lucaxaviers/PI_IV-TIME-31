/*
  Autor: Tiago Medeiros | 25000845
  Descricao: Comunicado enviado pelo Cliente para simular diarias, frete, caucao e total da locacao
*/

public class PedidoDeSimulacaoDeLocacao extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private int    codigoMaquina;
    private int    quantidadeDias;
    private double distanciaKm;

    public PedidoDeSimulacaoDeLocacao (int codigoMaquina,
                                       int quantidadeDias,
                                       double distanciaKm) throws Exception
    {
        if (codigoMaquina <= 0)
            throw new Exception("Codigo da maquina invalido");

        if (quantidadeDias <= 0)
            throw new Exception("Quantidade de dias deve ser positiva");

        if (distanciaKm < 0)
            throw new Exception("Distancia em km nao pode ser negativa");

        this.codigoMaquina  = codigoMaquina;
        this.quantidadeDias = quantidadeDias;
        this.distanciaKm    = distanciaKm;
    }

    public int getCodigoMaquina ()
    {
        return this.codigoMaquina;
    }

    public int getQuantidadeDias ()
    {
        return this.quantidadeDias;
    }

    public double getDistanciaKm ()
    {
        return this.distanciaKm;
    }

    @Override
    public String toString ()
    {
        return "Simulacao[maquina=" + this.codigoMaquina
                + ", dias=" + this.quantidadeDias
                + ", distanciaKm=" + this.distanciaKm + "]";
    }
}
