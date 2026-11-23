/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Comunicado enviado pelo Cliente para cadastrar uma nova maquina no catalogo
*/

public class PedidoDeCadastroDeMaquina extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private String tituloAnuncio;
    private String tipoEquipamento;
    private String municipio;
    private double valorDiaria;
    private String nomeLocador;

    public PedidoDeCadastroDeMaquina (String tituloAnuncio,
                                      String tipoEquipamento,
                                      String municipio,
                                      double valorDiaria,
                                      String nomeLocador) throws Exception
    {
        if (tituloAnuncio == null || tituloAnuncio.trim().isEmpty())
            throw new Exception("Titulo do anuncio ausente");

        if (tipoEquipamento == null || tipoEquipamento.trim().isEmpty())
            throw new Exception("Tipo do equipamento ausente");

        if (municipio == null || municipio.trim().isEmpty())
            throw new Exception("Municipio ausente");

        if (valorDiaria <= 0)
            throw new Exception("Valor da diaria deve ser positivo");

        if (nomeLocador == null || nomeLocador.trim().isEmpty())
            throw new Exception("Nome do locador ausente");

        this.tituloAnuncio   = tituloAnuncio.trim();
        this.tipoEquipamento = tipoEquipamento.trim();
        this.municipio       = municipio.trim();
        this.valorDiaria     = valorDiaria;
        this.nomeLocador     = nomeLocador.trim();
    }

    public String getTituloAnuncio ()
    {
        return this.tituloAnuncio;
    }

    public String getTipoEquipamento ()
    {
        return this.tipoEquipamento;
    }

    public String getMunicipio ()
    {
        return this.municipio;
    }

    public double getValorDiaria ()
    {
        return this.valorDiaria;
    }

    public String getNomeLocador ()
    {
        return this.nomeLocador;
    }

    @Override
    public String toString ()
    {
        return "Cadastro[" + this.tituloAnuncio + " - " + this.tipoEquipamento
                + " - " + this.municipio + " - R$ " + this.valorDiaria + "/dia]";
    }
}
