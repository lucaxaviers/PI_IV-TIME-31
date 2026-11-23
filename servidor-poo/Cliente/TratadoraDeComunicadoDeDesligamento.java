/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Thread auxiliar do Cliente que monitora o aviso de desligamento do Servidor
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
                if (this.servidor.espie() instanceof ComunicadoDeDesligamento)
                {
                    System.out.println("\nO servidor vai ser desligado agora;");
                    System.err.println("volte mais tarde!\n");
                    System.exit(0);
                }
                Thread.sleep(20);
            }
            catch (Exception erro)
            {
                return;
            }
        }
    }
}
