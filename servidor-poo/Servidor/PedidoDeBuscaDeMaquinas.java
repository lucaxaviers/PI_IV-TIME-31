/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Comunicado enviado pelo Cliente para buscar ou listar maquinas agricolas
*/

/**
 * Transporta os filtros opcionais (municipio e tipo) para consulta no catalogo do servidor.
 */
public class PedidoDeBuscaDeMaquinas extends Comunicado
{
    private static final long serialVersionUID = 1L;

    private String municipio;
    private String tipoEquipamento;

    // Strings vazias indicam ausencia de filtro (retorna todas as maquinas)
    public PedidoDeBuscaDeMaquinas (String municipio, String tipoEquipamento)
    {
        this.municipio       = (municipio == null ? "" : municipio.trim());
        this.tipoEquipamento = (tipoEquipamento == null ? "" : tipoEquipamento.trim());
    }

    public String getMunicipio ()
    {
        return this.municipio;
    }

    public String getTipoEquipamento ()
    {
        return this.tipoEquipamento;
    }

    @Override
    public String toString ()
    {
        return "Busca[municipio=" + this.municipio + ", tipo=" + this.tipoEquipamento + "]";
    }
}
