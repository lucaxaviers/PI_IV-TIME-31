/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Classe principal do Cliente TCP em console para operar o Servidor AlugaMaquina (zero HTTP)
*/

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Cliente TCP em modo texto dedicado ao AlugaMaquina.
 * Conecta no Servidor via Socket, abre fluxos de objetos e executa o ciclo receba -> espie -> envie.
 */
public class Cliente
{
    public static final String HOST_PADRAO  = "localhost";
    public static final int    PORTA_PADRAO = 3000;

    public static void main (String[] args)
    {
        // Aceita host e porta opcionais na linha de comando: java Cliente [HOST [PORTA]]
        if (args.length > 2)
        {
            System.err.println("Uso esperado: java Cliente [HOST [PORTA]]\n");
            return;
        }

        // 1. Estabelece a conexao TCP com o Servidor
        Socket conexao = null;
        try
        {
            String host = Cliente.HOST_PADRAO;
            int    porta = Cliente.PORTA_PADRAO;

            if (args.length > 0)
                host = args[0];

            if (args.length == 2)
                porta = Integer.parseInt(args[1]);

            conexao = new Socket(host, porta);
        }
        catch (Exception erro)
        {
            System.err.println("Indique o servidor e a porta corretos!\n");
            return;
        }

        // 2. Abre ObjectOutputStream antes do ObjectInputStream e faz flush para evitar deadlock
        ObjectOutputStream transmissor = null;
        try
        {
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
            transmissor.flush();
        }
        catch (Exception erro)
        {
            System.err.println("Indique o servidor e a porta corretos!\n");
            return;
        }

        ObjectInputStream receptor = null;
        try
        {
            receptor = new ObjectInputStream(conexao.getInputStream());
        }
        catch (Exception erro)
        {
            System.err.println("Indique o servidor e a porta corretos!\n");
            return;
        }

        // 3. Encapsula a conexao no objeto Parceiro e inicia a thread de desligamento
        Parceiro servidor = null;
        try
        {
            servidor = new Parceiro(conexao, receptor, transmissor);
        }
        catch (Exception erro)
        {
            System.err.println("Indique o servidor e a porta corretos!\n");
            return;
        }

        TratadoraDeComunicadoDeDesligamento tratadora = null;
        try
        {
            tratadora = new TratadoraDeComunicadoDeDesligamento(servidor);
        }
        catch (Exception erro)
        {} // sei que servidor foi instanciado

        tratadora.start();

        // 4. Laco do menu interativo de operacoes do AlugaMaquina
        char opcao = ' ';
        do
        {
            System.out.println("\n=== AlugaMaquina - Cliente TCP (POO/Java) ===");
            System.out.println("[1] Buscar / listar maquinas disponiveis");
            System.out.println("[2] Cadastrar nova maquina agricola");
            System.out.println("[3] Simular locacao e frete");
            System.out.println("[4] Registrar avaliacao pos-locacao");
            System.out.println("[5] Consultar reputacao de locador");
            System.out.println("[T] Terminar");
            System.out.print("Sua opcao? ");

            String linhaOpcao = Teclado.getUmString();
            if (linhaOpcao == null)
            {
                opcao = 'T';
                break;
            }

            linhaOpcao = linhaOpcao.trim().toUpperCase();
            if (linhaOpcao.length() != 1 || "12345T".indexOf(linhaOpcao.charAt(0)) == -1)
            {
                System.err.println("Opcao invalida!\n");
                continue;
            }

            opcao = linhaOpcao.charAt(0);

            try
            {
                if (opcao == '1')
                {
                    // Busca por municipio e/ou tipo (campos vazios listam todo o catalogo)
                    System.out.print("Municipio (deixe vazio para todos): ");
                    String municipio = lerTextoOpcional();

                    System.out.print("Tipo (trator/colheitadeira/pulverizador/plantadeira/implemento ou vazio): ");
                    String tipo = lerTextoOpcional();

                    servidor.receba(new PedidoDeBuscaDeMaquinas(municipio, tipo));
                    ResultadoDaOperacao resposta = aguardarOperacao(servidor);
                    System.out.println("\n" + resposta + "\n");
                }
                else if (opcao == '2')
                {
                    // Cadastro de novo anuncio de equipamento agricola
                    System.out.print("Titulo do anuncio: ");
                    String titulo = lerTextoObrigatorio();
                    System.out.print("Tipo (trator/colheitadeira/pulverizador/plantadeira/implemento): ");
                    String tipo = lerTextoObrigatorio();
                    System.out.print("Municipio: ");
                    String municipio = lerTextoObrigatorio();
                    System.out.print("Valor da diaria (R$): ");
                    double diaria = Teclado.getUmDouble();
                    System.out.print("Nome do locador: ");
                    String locador = lerTextoObrigatorio();

                    servidor.receba(new PedidoDeCadastroDeMaquina(titulo, tipo, municipio, diaria, locador));
                    ResultadoDaOperacao resposta = aguardarOperacao(servidor);
                    System.out.println("\n" + resposta + "\n");
                }
                else if (opcao == '3')
                {
                    // Simulacao financeira de diarias, frete (ida e volta + mobilizacao) e caucao
                    System.out.print("Codigo da maquina (#): ");
                    int codigo = Teclado.getUmInt();
                    System.out.print("Quantidade de dias: ");
                    int dias = Teclado.getUmInt();
                    System.out.print("Distancia para frete (km): ");
                    double distanciaKm = Teclado.getUmDouble();

                    servidor.receba(new PedidoDeSimulacaoDeLocacao(codigo, dias, distanciaKm));
                    ResultadoDaSimulacao resposta = aguardarSimulacao(servidor);
                    System.out.println("\n" + resposta + "\n");
                }
                else if (opcao == '4')
                {
                    // Registro de avaliacao pos-locacao com 5 criterios de 1 a 5 estrelas
                    System.out.print("Nome do locador avaliado: ");
                    String locador = lerTextoObrigatorio();
                    System.out.print("Nota Desempenho (1 a 5): ");
                    int n1 = Teclado.getUmInt();
                    System.out.print("Nota Conservacao (1 a 5): ");
                    int n2 = Teclado.getUmInt();
                    System.out.print("Nota Fidelidade (1 a 5): ");
                    int n3 = Teclado.getUmInt();
                    System.out.print("Nota Pontualidade (1 a 5): ");
                    int n4 = Teclado.getUmInt();
                    System.out.print("Nota Suporte (1 a 5): ");
                    int n5 = Teclado.getUmInt();
                    System.out.print("Comentario: ");
                    String comentario = lerTextoOpcional();
                    System.out.print("Recomenda o locador (S/N)? ");
                    boolean recomenda = lerSimNao();

                    servidor.receba(new PedidoDeAvaliacao(locador, n1, n2, n3, n4, n5, comentario, recomenda));
                    ResultadoDaOperacao resposta = aguardarOperacao(servidor);
                    System.out.println("\n" + resposta + "\n");
                }
                else if (opcao == '5')
                {
                    // Consulta da reputacao media atualizada de um locador
                    System.out.print("Nome do locador: ");
                    String locador = lerTextoObrigatorio();

                    servidor.receba(new PedidoDeConsultaDeReputacao(locador));
                    ResultadoDaOperacao resposta = aguardarOperacao(servidor);
                    System.out.println("\n" + resposta + "\n");
                }
            }
            catch (Exception erro)
            {
                System.err.println("\nErro na operacao: " + erro.getMessage() + "\n");
            }
        }
        while (opcao != 'T');

        // 5. Envia o pedido de encerramento para que a SupervisoraDeConexao remova o cliente
        try
        {
            servidor.receba(new PedidoParaSair());
            servidor.adeus();
        }
        catch (Exception erro)
        {}

        System.out.println("Obrigado por usar o AlugaMaquina!");
        System.exit(0);
    }

    // Padrao do Fazedor de Continhas: espia ate chegar ResultadoDaOperacao e entao consome
    private static ResultadoDaOperacao aguardarOperacao (Parceiro servidor) throws Exception
    {
        Comunicado comunicado;
        do
        {
            comunicado = servidor.espie();
        }
        while (!(comunicado instanceof ResultadoDaOperacao));

        return (ResultadoDaOperacao) servidor.envie();
    }

    // Padrao do Fazedor de Continhas: espia ate chegar ResultadoDaSimulacao e entao consome
    private static ResultadoDaSimulacao aguardarSimulacao (Parceiro servidor) throws Exception
    {
        Comunicado comunicado;
        do
        {
            comunicado = servidor.espie();
        }
        while (!(comunicado instanceof ResultadoDaSimulacao));

        return (ResultadoDaSimulacao) servidor.envie();
    }

    private static String lerTextoOpcional ()
    {
        String texto = Teclado.getUmString();
        return (texto == null ? "" : texto.trim());
    }

    private static String lerTextoObrigatorio () throws Exception
    {
        String texto = Teclado.getUmString();
        if (texto == null || texto.trim().isEmpty())
            throw new Exception("Campo obrigatorio nao preenchido");

        return texto.trim();
    }

    private static boolean lerSimNao () throws Exception
    {
        String texto = Teclado.getUmString();
        if (texto == null)
            throw new Exception("Resposta invalida (use S ou N)");

        String norm = texto.trim().toUpperCase();
        if (norm.equals("S") || norm.equals("SIM") || norm.equals("TRUE"))
            return true;
        if (norm.equals("N") || norm.equals("NAO") || norm.equals("FALSE"))
            return false;

        throw new Exception("Resposta invalida (use S ou N)");
    }
}
