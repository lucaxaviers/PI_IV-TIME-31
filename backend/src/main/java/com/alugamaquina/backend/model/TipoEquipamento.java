/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Tipos de equipamento do cadastro de maquinas e a chave usada pelo catalogo (plural) ao gravar no banco
*/

package com.alugamaquina.backend.model;

/**
 * Tipos da lista do formulario de cadastro. O catalogo agrupa pelo plural
 * (tratores, colheitadeiras...), igual aos dados iniciais do MongoDB.
 */
public enum TipoEquipamento {

    TRATOR("trator", "tratores"),
    COLHEITADEIRA("colheitadeira", "colheitadeiras"),
    PULVERIZADOR("pulverizador", "pulverizadores"),
    PLANTADEIRA("plantadeira", "plantadeiras"),
    IMPLEMENTO("implemento", "implementos");

    private final String codigo;
    private final String chaveCatalogo;

    TipoEquipamento(String codigo, String chaveCatalogo) {
        this.codigo = codigo;
        this.chaveCatalogo = chaveCatalogo;
    }

    // Codigo igual ao value do select do formulario (ex.: "trator")
    public String getCodigo() {
        return codigo;
    }

    // Valor gravado em tipoEquipamento (ex.: "tratores")
    public String getChaveCatalogo() {
        return chaveCatalogo;
    }

    // Verdadeiro se o codigo existe na lista (aceita maiusculas e espacos nas pontas)
    public static boolean isCodigoValido(String codigo) {
        return TipoEquipamento.buscarPorCodigo(codigo) != null;
    }

    // Devolve o tipo do codigo, ou null se o codigo nao existe
    public static TipoEquipamento buscarPorCodigo(String codigo) {
        if (codigo == null)
            return null;

        String procurado = codigo.trim();
        for (TipoEquipamento tipo : TipoEquipamento.values()) {
            if (tipo.codigo.equalsIgnoreCase(procurado))
                return tipo;
        }
        return null;
    }
}
