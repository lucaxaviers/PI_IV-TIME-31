/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Comunicado devolvido pelo Servidor com resultado textual de busca, cadastro ou avaliacao
*/

/**
 * Resposta generica retornada pelo Servidor para operacoes de busca, cadastro, avaliacao e consulta.
 */
public class ResultadoDaOperacao extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private boolean sucesso;
    private String  mensagem;
    private String  detalhes;

    public ResultadoDaOperacao (boolean sucesso, String mensagem, String detalhes)
    {
        this.sucesso  = sucesso;
        this.mensagem = (mensagem == null ? "" : mensagem);
        this.detalhes = (detalhes == null ? "" : detalhes);
    }

    public boolean isSucesso ()
    {
        return this.sucesso;
    }

    public String getMensagem ()
    {
        return this.mensagem;
    }

    public String getDetalhes ()
    {
        return this.detalhes;
    }

    @Override
    public String toString ()
    {
        if (this.detalhes.isEmpty())
            return this.mensagem;

        return this.mensagem + "\n" + this.detalhes;
    }
}
