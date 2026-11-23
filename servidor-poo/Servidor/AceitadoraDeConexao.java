/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Thread que aceita conexoes TCP via ServerSocket e cria uma SupervisoraDeConexao por cliente
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Fica continuamente ouvindo a porta TCP configurada.
 * A cada novo cliente que conecta, delega o atendimento para uma SupervisoraDeConexao dedicada.
 */
public class AceitadoraDeConexao extends Thread
{
    private ServerSocket            pedido;
    private ArrayList<Parceiro>     usuarios;
    private RepositorioAlugaMaquina repositorio;

    // Abre o ServerSocket na porta informada e valida as dependencias compartilhadas
    public AceitadoraDeConexao (String porta,
                                ArrayList<Parceiro> usuarios,
                                RepositorioAlugaMaquina repositorio) throws Exception
    {
        if (porta == null)
            throw new Exception("Porta ausente");

        try
        {
            this.pedido = new ServerSocket(Integer.parseInt(porta));
        }
        catch (Exception erro)
        {
            throw new Exception("Porta invalida");
        }

        if (usuarios == null)
            throw new Exception("Usuarios ausentes");

        if (repositorio == null)
            throw new Exception("Repositorio ausente");

        this.usuarios    = usuarios;
        this.repositorio = repositorio;
    }

    // Laco infinito de aceitacao: cada conexao gera uma thread independente de supervisao
    @Override
    public void run ()
    {
        for (;;)
        {
            Socket conexao = null;
            try
            {
                // Aguarda bloqueado ate o proximo cliente conectar via TCP
                conexao = this.pedido.accept();
            }
            catch (Exception erro)
            {
                continue;
            }

            SupervisoraDeConexao supervisoraDeConexao = null;
            try
            {
                supervisoraDeConexao = new SupervisoraDeConexao(conexao, this.usuarios, this.repositorio);
            }
            catch (Exception erro)
            {} // sei que passei parametros corretos para o construtor

            supervisoraDeConexao.start();
        }
    }
}
