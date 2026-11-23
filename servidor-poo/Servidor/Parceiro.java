/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Encapsula conexao Socket TCP e fluxos de objetos entre Cliente e Servidor
*/

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Representa o outro lado da comunicacao TCP (o Cliente visto pelo Servidor, ou o Servidor visto pelo Cliente).
 * Usa um semaforo binario (mutEx) para permitir espiar e consumir comunicados entre threads sem conflito.
 */
public class Parceiro
{
    private Socket             conexao;
    private ObjectInputStream  receptor;
    private ObjectOutputStream transmissor;

    // Buffer de 1 posicao: guarda o comunicado lido por espie() ate ser consumido por envie()
    private Comunicado proximoComunicado = null;

    // Garante exclusao mutua justa (FIFO) entre a thread principal e a tratadora de desligamento
    private Semaphore mutEx = new Semaphore(1, true);

    public Parceiro (Socket             conexao,
                     ObjectInputStream  receptor,
                     ObjectOutputStream transmissor)
                     throws Exception // se parametro nulos
    {
        if (conexao == null)
            throw new Exception("Conexao ausente");

        if (receptor == null)
            throw new Exception("Receptor ausente");

        if (transmissor == null)
            throw new Exception("Transmissor ausente");

        this.conexao     = conexao;
        this.receptor    = receptor;
        this.transmissor = transmissor;
    }

    // Envia um Comunicado serializado para o outro lado da conexao TCP
    public void receba (Comunicado x) throws Exception
    {
        try
        {
            this.transmissor.writeObject(x);
            this.transmissor.flush();
        }
        catch (IOException erro)
        {
            throw new Exception("Erro de transmissao");
        }
    }

    // Le o proximo Comunicado sem retira-lo do buffer (permite inspecionar antes de consumir)
    public Comunicado espie () throws Exception
    {
        this.mutEx.acquireUninterruptibly();
        try
        {
            if (this.proximoComunicado == null)
                this.proximoComunicado = (Comunicado) this.receptor.readObject();

            return this.proximoComunicado;
        }
        catch (Exception erro)
        {
            throw new Exception("Erro de recepcao");
        }
        finally
        {
            this.mutEx.release();
        }
    }

    // Consome e devolve o proximo Comunicado disponivel, limpando o buffer para a proxima leitura
    public Comunicado envie () throws Exception
    {
        this.mutEx.acquireUninterruptibly();
        try
        {
            if (this.proximoComunicado == null)
                this.proximoComunicado = (Comunicado) this.receptor.readObject();

            Comunicado ret = this.proximoComunicado;
            this.proximoComunicado = null;
            return ret;
        }
        catch (Exception erro)
        {
            throw new Exception("Erro de recepcao");
        }
        finally
        {
            this.mutEx.release();
        }
    }

    // Fecha os fluxos de entrada/saida e encerra o Socket TCP
    public void adeus () throws Exception
    {
        try
        {
            this.transmissor.close();
            this.receptor.close();
            this.conexao.close();
        }
        catch (Exception erro)
        {
            throw new Exception("Erro de desconexao");
        }
    }
}
