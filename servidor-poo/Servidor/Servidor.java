/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Classe principal do Servidor TCP dedicado ao AlugaMaquina (zero HTTP)
*/

import java.util.ArrayList;

/**
 * Ponto de entrada do servidor TCP puro do AlugaMaquina.
 * Sobe o socket de escuta em thread dedicada e aguarda o comando "desativar" no console.
 */
public class Servidor
{
    // Porta padrao utilizada quando nenhum argumento e passado na linha de comando
    public static String PORTA_PADRAO = "3000";

    public static void main (String[] args)
    {
        // Aceita no maximo 1 argumento opcional (numero da porta)
        if (args.length > 1)
        {
            System.err.println("Uso esperado: java Servidor [PORTA]\n");
            return;
        }

        String porta = Servidor.PORTA_PADRAO;
        if (args.length == 1)
            porta = args[0];

        // Lista sincronizada com todos os clientes conectados no momento
        ArrayList<Parceiro> usuarios = new ArrayList<Parceiro>();

        // Repositorio compartilhado em memoria com catalogo, simulacoes e reputacao
        RepositorioAlugaMaquina repositorio = new RepositorioAlugaMaquina();

        // Thread que fica aceitando novas conexoes TCP na porta configurada
        AceitadoraDeConexao aceitadoraDeConexao = null;
        try
        {
            aceitadoraDeConexao = new AceitadoraDeConexao(porta, usuarios, repositorio);
            aceitadoraDeConexao.start();
        }
        catch (Exception erro)
        {
            System.err.println("Escolha uma porta apropriada e liberada para uso!\n");
            return;
        }

        // Loop administrativo no terminal do servidor para encerramento controlado
        for (;;)
        {
            System.out.println("O servidor AlugaMaquina esta ativo na porta " + porta + "! Para desativa-lo,");
            System.out.println("use o comando \"desativar\"\n");
            System.out.print("> ");

            String comando = Teclado.getUmString();
            if (comando == null)
                break;

            if (comando.trim().equalsIgnoreCase("desativar"))
            {
                // Avisa todos os clientes ativos antes de fechar os sockets e encerrar a JVM
                synchronized (usuarios)
                {
                    ComunicadoDeDesligamento comunicadoDeDesligamento =
                        new ComunicadoDeDesligamento();

                    for (Parceiro usuario : usuarios)
                    {
                        try
                        {
                            usuario.receba(comunicadoDeDesligamento);
                            usuario.adeus();
                        }
                        catch (Exception erro)
                        {} // ignora cliente que ja tenha desconectado
                    }
                }

                System.out.println("O servidor foi desativado!\n");
                System.exit(0);
            }
            else
            {
                System.err.println("Comando invalido!\n");
            }
        }
    }
}
