/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Comunicado enviado pelo Cliente para registrar avaliacao pos-locacao de um locador
*/

/**
 * Transporta as 5 notas (1 a 5), comentario e recomendacao para avaliacao pos-locacao.
 */
public class PedidoDeAvaliacao extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private String  nomeLocador;
    private int     notaDesempenho;
    private int     notaConservacao;
    private int     notaFidelidade;
    private int     notaPontualidade;
    private int     notaSuporte;
    private String  comentario;
    private boolean recomenda;

    // Garante que o locador foi informado e todas as 5 notas estao na faixa de 1 a 5
    public PedidoDeAvaliacao (String nomeLocador,
                              int notaDesempenho,
                              int notaConservacao,
                              int notaFidelidade,
                              int notaPontualidade,
                              int notaSuporte,
                              String comentario,
                              boolean recomenda) throws Exception
    {
        if (nomeLocador == null || nomeLocador.trim().isEmpty())
            throw new Exception("Nome do locador ausente");

        validarNota("Desempenho", notaDesempenho);
        validarNota("Conservacao", notaConservacao);
        validarNota("Fidelidade", notaFidelidade);
        validarNota("Pontualidade", notaPontualidade);
        validarNota("Suporte", notaSuporte);

        this.nomeLocador      = nomeLocador.trim();
        this.notaDesempenho   = notaDesempenho;
        this.notaConservacao  = notaConservacao;
        this.notaFidelidade   = notaFidelidade;
        this.notaPontualidade = notaPontualidade;
        this.notaSuporte      = notaSuporte;
        this.comentario       = (comentario == null ? "" : comentario.trim());
        this.recomenda        = recomenda;
    }

    // Impede notas fora da escala de 1 a 5 estrelas
    private static void validarNota (String criterio, int nota) throws Exception
    {
        if (nota < 1 || nota > 5)
            throw new Exception("A nota de " + criterio + " deve estar entre 1 e 5");
    }

    public String getNomeLocador ()
    {
        return this.nomeLocador;
    }

    public int getNotaDesempenho ()
    {
        return this.notaDesempenho;
    }

    public int getNotaConservacao ()
    {
        return this.notaConservacao;
    }

    public int getNotaFidelidade ()
    {
        return this.notaFidelidade;
    }

    public int getNotaPontualidade ()
    {
        return this.notaPontualidade;
    }

    public int getNotaSuporte ()
    {
        return this.notaSuporte;
    }

    public String getComentario ()
    {
        return this.comentario;
    }

    public boolean isRecomenda ()
    {
        return this.recomenda;
    }

    @Override
    public String toString ()
    {
        return "Avaliacao[locador=" + this.nomeLocador + ", recomenda=" + this.recomenda + "]";
    }
}
