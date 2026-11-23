/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Thread dedicada por cliente conectado para processar comunicados do AlugaMaquina
*/

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SupervisoraDeConexao extends Thread
{
    private Parceiro                usuario;
    private Socket                  conexao;
    private ArrayList<Parceiro>     usuarios;
    private RepositorioAlugaMaquina repositorio;

    public SupervisoraDeConexao (Socket conexao,
                                 ArrayList<Parceiro> usuarios,
                                 RepositorioAlugaMaquina repositorio) throws Exception
    {
        if (conexao == null)
            throw new Exception("Conexao ausente");

        if (usuarios == null)
            throw new Exception("Usuarios ausentes");

        if (repositorio == null)
            throw new Exception("Repositorio ausente");

        this.conexao     = conexao;
        this.usuarios    = usuarios;
        this.repositorio = repositorio;
    }

    @Override
    public void run ()
    {
        ObjectOutputStream transmissor;
        try
        {
            transmissor = new ObjectOutputStream(this.conexao.getOutputStream());
            transmissor.flush();
        }
        catch (Exception erro)
        {
            return;
        }

        ObjectInputStream receptor = null;
        try
        {
            receptor = new ObjectInputStream(this.conexao.getInputStream());
        }
        catch (Exception erro)
        {
            try
            {
                transmissor.close();
            }
            catch (Exception falha)
            {} // fechando antes de encerrar a thread

            return;
        }

        try
        {
            this.usuario = new Parceiro(this.conexao, receptor, transmissor);
        }
        catch (Exception erro)
        {} // sei que os parametros foram validados

        try
        {
            synchronized (this.usuarios)
            {
                this.usuarios.add(this.usuario);
            }

            for (;;)
            {
                Comunicado comunicado = this.usuario.envie();

                if (comunicado == null)
                    break;
                else if (comunicado instanceof PedidoDeBuscaDeMaquinas)
                {
                    PedidoDeBuscaDeMaquinas pedido = (PedidoDeBuscaDeMaquinas) comunicado;
                    this.usuario.receba(this.repositorio.buscarMaquinas(pedido));
                }
                else if (comunicado instanceof PedidoDeCadastroDeMaquina)
                {
                    PedidoDeCadastroDeMaquina pedido = (PedidoDeCadastroDeMaquina) comunicado;
                    this.usuario.receba(this.repositorio.cadastrarMaquina(pedido));
                }
                else if (comunicado instanceof PedidoDeSimulacaoDeLocacao)
                {
                    PedidoDeSimulacaoDeLocacao pedido = (PedidoDeSimulacaoDeLocacao) comunicado;
                    this.usuario.receba(this.repositorio.simularLocacao(pedido));
                }
                else if (comunicado instanceof PedidoDeAvaliacao)
                {
                    PedidoDeAvaliacao pedido = (PedidoDeAvaliacao) comunicado;
                    this.usuario.receba(this.repositorio.registrarAvaliacao(pedido));
                }
                else if (comunicado instanceof PedidoDeConsultaDeReputacao)
                {
                    PedidoDeConsultaDeReputacao pedido = (PedidoDeConsultaDeReputacao) comunicado;
                    this.usuario.receba(this.repositorio.consultarReputacao(pedido));
                }
                else if (comunicado instanceof PedidoParaSair)
                {
                    synchronized (this.usuarios)
                    {
                        this.usuarios.remove(this.usuario);
                    }
                    this.usuario.adeus();
                    return;
                }
            }
        }
        catch (Exception erro)
        {
            synchronized (this.usuarios)
            {
                this.usuarios.remove(this.usuario);
            }
            try
            {
                if (this.usuario != null)
                    this.usuario.adeus();
            }
            catch (Exception falha)
            {} // fechando antes de encerrar a thread
        }
    }
}
