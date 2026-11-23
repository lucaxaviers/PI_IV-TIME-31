/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Comunicado enviado pelo Cliente para consultar a reputacao acumulada de um locador
*/

/**
 * Solicita ao servidor o historico agregado de notas (reputacao media) de um locador.
 */
public class PedidoDeConsultaDeReputacao extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private String nomeLocador;

    // Exige o nome do locador nao vazio para pesquisa no repositorio
    public PedidoDeConsultaDeReputacao (String nomeLocador) throws Exception
    {
        if (nomeLocador == null || nomeLocador.trim().isEmpty())
            throw new Exception("Nome do locador ausente");

        this.nomeLocador = nomeLocador.trim();
    }

    public String getNomeLocador ()
    {
        return this.nomeLocador;
    }

    @Override
    public String toString ()
    {
        return "ConsultaReputacao[locador=" + this.nomeLocador + "]";
    }
}
