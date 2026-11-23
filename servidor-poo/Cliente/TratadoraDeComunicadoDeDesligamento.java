/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Thread auxiliar do Cliente que monitora o aviso de desligamento do Servidor
*/

/**
 * Executa em segundo plano verificando se o Servidor enviou ComunicadoDeDesligamento.
 * Caso receba o aviso, notifica o usuario no console e encerra o programa cliente imediatamente.
 */
public class TratadoraDeComunicadoDeDesligamento extends Thread
{
    private Parceiro servidor;

    public TratadoraDeComunicadoDeDesligamento (Parceiro servidor) throws Exception
    {
        if (servidor == null)
            throw new Exception("Servidor invalido");

        this.servidor = servidor;
    }

    @Override
    public void run ()
    {
        for (;;)
        {
            try
            {
                // Espia sem consumir o proximo comunicado para nao interferir nas respostas do menu
                if (this.servidor.espie() instanceof ComunicadoDeDesligamento)
                {
                    System.out.println("\nO servidor vai ser desligado agora;");
                    System.err.println("volte mais tarde!\n");
                    System.exit(0);
                }
                // Breve pausa para ceder o semaforo do Parceiro a thread principal quando houver resposta pendente
                Thread.sleep(20);
            }
            catch (Exception erro)
            {
                // Sai silenciosamente se o socket for fechado durante o encerramento normal do Cliente
                return;
            }
        }
    }
}
