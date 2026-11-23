/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Repositorio compartilhado e thread-safe com as regras de negocio do AlugaMaquina
*/

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositorioAlugaMaquina
{
    public static final double VALOR_POR_KM        = 6.50;
    public static final double TAXA_MOBILIZACAO    = 150.00;
    public static final double VALOR_CAUCAO_PADRAO = 1000.00;

    private final List<MaquinaAgricola> maquinas = new ArrayList<MaquinaAgricola>();
    private final Map<String, List<Double>> historicoNotas = new HashMap<String, List<Double>>();
    private int proximoCodigo = 1;

    public RepositorioAlugaMaquina ()
    {
        inicializarCatalogoPiloto();
    }

    private void inicializarCatalogoPiloto ()
    {
        try
        {
            adicionarInicial("Trator John Deere 6125J", "trator", "Paulinia", 850.00, "Fazenda Sao Joao");
            adicionarInicial("Colheitadeira Case IH Axial-Flow", "colheitadeira", "Campinas", 1950.00, "Agropecuaria Campinas");
            adicionarInicial("Pulverizador Jacto Uniport", "pulverizador", "Cosmopolis", 1100.00, "Sitio Boa Vista");
            adicionarInicial("Plantadeira Stara Princesa", "plantadeira", "Sumare", 920.00, "Fazenda Santa Clara");
            adicionarInicial("Grade Aradora Baldan", "implemento", "Holambra", 420.00, "Cooperativa Holambra");
        }
        catch (Exception erro)
        {} // dados iniciais validos
    }

    private void adicionarInicial (String titulo,
                                     String tipo,
                                     String municipio,
                                     double diaria,
                                     String locador) throws Exception
    {
        this.maquinas.add(new MaquinaAgricola(this.proximoCodigo++, titulo, tipo, municipio, diaria, locador));
        garantirLocador(locador);
    }

    private void garantirLocador (String nomeLocador)
    {
        String chave = nomeLocador.trim().toLowerCase();
        if (!this.historicoNotas.containsKey(chave))
        {
            List<Double> notas = new ArrayList<Double>();
            notas.add(5.0);
            this.historicoNotas.put(chave, notas);
        }
    }

    public synchronized ResultadoDaOperacao buscarMaquinas (PedidoDeBuscaDeMaquinas pedido)
    {
        String filtroMunicipio = (pedido == null ? "" : pedido.getMunicipio().toLowerCase());
        String filtroTipo      = (pedido == null ? "" : pedido.getTipoEquipamento().toLowerCase());

        StringBuilder sb = new StringBuilder();
        int encontradas = 0;

        for (MaquinaAgricola maq : this.maquinas)
        {
            boolean bateMunicipio = filtroMunicipio.isEmpty()
                || maq.getMunicipio().toLowerCase().contains(filtroMunicipio);
            boolean bateTipo = filtroTipo.isEmpty()
                || maq.getTipoEquipamento().toLowerCase().contains(filtroTipo);

            if (bateMunicipio && bateTipo)
            {
                if (encontradas > 0) sb.append("\n");
                sb.append(maq.toString());
                encontradas++;
            }
        }

        if (encontradas == 0)
        {
            return new ResultadoDaOperacao(
                true,
                "Nenhuma maquina encontrada para os filtros informados.",
                ""
            );
        }

        return new ResultadoDaOperacao(
            true,
            encontradas + " maquina(s) encontrada(s) no catalogo:",
            sb.toString()
        );
    }

    public synchronized ResultadoDaOperacao cadastrarMaquina (PedidoDeCadastroDeMaquina pedido)
    {
        try
        {
            MaquinaAgricola nova = new MaquinaAgricola(
                this.proximoCodigo++,
                pedido.getTituloAnuncio(),
                pedido.getTipoEquipamento(),
                pedido.getMunicipio(),
                pedido.getValorDiaria(),
                pedido.getNomeLocador()
            );
            this.maquinas.add(nova);
            garantirLocador(pedido.getNomeLocador());

            return new ResultadoDaOperacao(
                true,
                "Maquina cadastrada com sucesso no servidor!",
                nova.toString()
            );
        }
        catch (Exception erro)
        {
            return new ResultadoDaOperacao(false, "Erro ao cadastrar maquina: " + erro.getMessage(), "");
        }
    }

    public synchronized ResultadoDaSimulacao simularLocacao (PedidoDeSimulacaoDeLocacao pedido)
    {
        MaquinaAgricola encontrada = null;
        for (MaquinaAgricola maq : this.maquinas)
        {
            if (maq.getCodigo() == pedido.getCodigoMaquina())
            {
                encontrada = maq;
                break;
            }
        }

        if (encontrada == null)
        {
            return new ResultadoDaSimulacao(
                false,
                "Maquina codigo #" + pedido.getCodigoMaquina() + " nao encontrada.",
                "", 0, 0, 0, 0, 0, 0
            );
        }

        double subtotal = arredondar(encontrada.getValorDiaria() * pedido.getQuantidadeDias());
        double frete    = arredondar(pedido.getDistanciaKm() * 2.0 * VALOR_POR_KM + TAXA_MOBILIZACAO);
        double total    = arredondar(subtotal + frete);

        return new ResultadoDaSimulacao(
            true,
            "Simulacao calculada com sucesso.",
            encontrada.getTituloAnuncio() + " (" + encontrada.getMunicipio() + ")",
            pedido.getQuantidadeDias(),
            pedido.getDistanciaKm(),
            subtotal,
            frete,
            VALOR_CAUCAO_PADRAO,
            total
        );
    }

    public synchronized ResultadoDaOperacao registrarAvaliacao (PedidoDeAvaliacao pedido)
    {
        String chave = pedido.getNomeLocador().trim().toLowerCase();
        List<Double> notas = this.historicoNotas.get(chave);
        if (notas == null)
        {
            notas = new ArrayList<Double>();
            this.historicoNotas.put(chave, notas);
        }

        double somaCriterios = pedido.getNotaDesempenho()
                             + pedido.getNotaConservacao()
                             + pedido.getNotaFidelidade()
                             + pedido.getNotaPontualidade()
                             + pedido.getNotaSuporte();

        double mediaIndividual = arredondar(somaCriterios / 5.0);
        notas.add(mediaIndividual);

        double reputacaoAcumulada = calcularMedia(notas);
        String detalhes = String.format(
            "Locador: %s | Nota desta avaliacao: %.2f | Nova reputacao: %.2f (%d avaliacao(oes)) | Recomenda: %s",
            pedido.getNomeLocador(),
            mediaIndividual,
            reputacaoAcumulada,
            notas.size(),
            (pedido.isRecomenda() ? "Sim" : "Nao")
        );

        return new ResultadoDaOperacao(true, "Avaliacao registrada com sucesso!", detalhes);
    }

    public synchronized ResultadoDaOperacao consultarReputacao (PedidoDeConsultaDeReputacao pedido)
    {
        String chave = pedido.getNomeLocador().trim().toLowerCase();
        List<Double> notas = this.historicoNotas.get(chave);

        if (notas == null || notas.isEmpty())
        {
            return new ResultadoDaOperacao(
                false,
                "Locador nao encontrado na base de reputacao: " + pedido.getNomeLocador(),
                ""
            );
        }

        double media = calcularMedia(notas);
        String detalhes = String.format(
            "Locador: %s | Reputacao media: %.2f | Total de registros: %d",
            pedido.getNomeLocador(), media, notas.size()
        );

        return new ResultadoDaOperacao(true, "Consulta de reputacao concluida.", detalhes);
    }

    private static double calcularMedia (List<Double> notas)
    {
        double soma = 0.0;
        for (double nota : notas)
            soma += nota;

        return arredondar(soma / notas.size());
    }

    private static double arredondar (double valor)
    {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
