/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Classe de dominio MaquinaAgricola com metodos canonicos completos (padrao POO/Java)
*/

import java.io.Serializable;
import java.util.Objects;

public class MaquinaAgricola implements Serializable, Comparable<MaquinaAgricola>, Cloneable
{
    private static final long serialVersionUID = 1L;

    private int    codigo;
    private String tituloAnuncio;
    private String tipoEquipamento;
    private String municipio;
    private double valorDiaria;
    private String nomeLocador;

    public MaquinaAgricola (int codigo,
                            String tituloAnuncio,
                            String tipoEquipamento,
                            String municipio,
                            double valorDiaria,
                            String nomeLocador) throws Exception
    {
        if (codigo <= 0)
            throw new Exception("Codigo da maquina invalido");

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

        this.codigo          = codigo;
        this.tituloAnuncio   = tituloAnuncio.trim();
        this.tipoEquipamento = tipoEquipamento.trim();
        this.municipio       = municipio.trim();
        this.valorDiaria     = valorDiaria;
        this.nomeLocador     = nomeLocador.trim();
    }

    public int getCodigo ()
    {
        return this.codigo;
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

    public void setValorDiaria (double valorDiaria) throws Exception
    {
        if (valorDiaria <= 0)
            throw new Exception("Valor da diaria deve ser positivo");

        this.valorDiaria = valorDiaria;
    }

    // ---- Metodos canonicos (padrao da disciplina POO/Java) ----

    public MaquinaAgricola (MaquinaAgricola modelo) throws Exception
    {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.codigo          = modelo.codigo;
        this.tituloAnuncio   = modelo.tituloAnuncio;
        this.tipoEquipamento = modelo.tipoEquipamento;
        this.municipio       = modelo.municipio;
        this.valorDiaria     = modelo.valorDiaria;
        this.nomeLocador     = modelo.nomeLocador;
    }

    @Override
    public Object clone ()
    {
        MaquinaAgricola retorno = null;

        try
        {
            retorno = new MaquinaAgricola(this);
        }
        catch (Exception erro)
        {} // sei que nao vai dar erro: o this nunca e nulo

        return retorno;
    }

    @Override
    public String toString ()
    {
        return String.format(
            "[#%d] %s (%s) | %s | R$ %.2f/dia | Locador: %s",
            this.codigo, this.tituloAnuncio, this.tipoEquipamento,
            this.municipio, this.valorDiaria, this.nomeLocador
        );
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        MaquinaAgricola maq = (MaquinaAgricola) obj;

        if (this.codigo != maq.codigo) return false;
        if (!Objects.equals(this.tituloAnuncio, maq.tituloAnuncio)) return false;
        if (!Objects.equals(this.tipoEquipamento, maq.tipoEquipamento)) return false;
        if (!Objects.equals(this.municipio, maq.municipio)) return false;
        if (Double.compare(this.valorDiaria, maq.valorDiaria) != 0) return false;
        if (!Objects.equals(this.nomeLocador, maq.nomeLocador)) return false;

        return true;
    }

    @Override
    public int hashCode ()
    {
        int retorno = 1;

        retorno = retorno * 2 + Integer.hashCode(this.codigo);
        retorno = retorno * 2 + (this.tituloAnuncio == null ? 0 : this.tituloAnuncio.hashCode());
        retorno = retorno * 2 + (this.tipoEquipamento == null ? 0 : this.tipoEquipamento.hashCode());
        retorno = retorno * 2 + (this.municipio == null ? 0 : this.municipio.hashCode());
        retorno = retorno * 2 + Double.hashCode(this.valorDiaria);
        retorno = retorno * 2 + (this.nomeLocador == null ? 0 : this.nomeLocador.hashCode());

        if (retorno < 0) retorno = -retorno;
        return retorno;
    }

    @Override
    public int compareTo (MaquinaAgricola outra)
    {
        if (this.codigo < outra.codigo) return -666;
        if (this.codigo > outra.codigo) return 666;
        return 0;
    }
}
