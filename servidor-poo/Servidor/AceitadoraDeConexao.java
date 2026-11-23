/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Thread que aceita conexoes TCP via ServerSocket e cria uma SupervisoraDeConexao por cliente
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AceitadoraDeConexao extends Thread
{
    private ServerSocket            pedido;
    private ArrayList<Parceiro>     usuarios;
    private RepositorioAlugaMaquina repositorio;

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

    @Override
    public void run ()
    {
        for (;;)
        {
            Socket conexao = null;
            try
            {
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
