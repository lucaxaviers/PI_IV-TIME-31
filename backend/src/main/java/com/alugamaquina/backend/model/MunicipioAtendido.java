/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Municipios atendidos no cadastro de maquinas, com o nome de exibicao e as coordenadas usadas na busca por raio
*/

package com.alugamaquina.backend.model;

/**
 * Municipios da lista do formulario de cadastro de maquinas.
 * As coordenadas (longitude, latitude) alimentam o indice 2dsphere da collection maquinas.
 */
public enum MunicipioAtendido {

    PAULINIA("paulinia", "Paulínia / SP", -47.1100, -22.7100),
    CAMPINAS("campinas", "Campinas / SP", -47.0608, -22.9056),
    COSMOPOLIS("cosmopolis", "Cosmópolis / SP", -47.1960, -22.6458),
    SUMARE("sumare", "Sumaré / SP", -47.2667, -22.8219),
    HOLAMBRA("holambra", "Holambra / SP", -47.0547, -22.6333),
    JAGUARIUNA("jaguariuna", "Jaguariúna / SP", -46.9858, -22.7055),
    ARTUR_NOGUEIRA("artur-nogueira", "Artur Nogueira / SP", -47.1725, -22.5731);

    private final String codigo;
    private final String nome;
    private final double longitude;
    private final double latitude;

    MunicipioAtendido(String codigo, String nome, double longitude, double latitude) {
        this.codigo = codigo;
        this.nome = nome;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Codigo igual ao value do select do formulario (ex.: "artur-nogueira")
    public String getCodigo() {
        return codigo;
    }

    // Nome como aparece no catalogo (ex.: "Paulínia / SP")
    public String getNome() {
        return nome;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    // Verdadeiro se o codigo existe na lista (aceita maiusculas e espacos nas pontas)
    public static boolean isCodigoValido(String codigo) {
        return MunicipioAtendido.buscarPorCodigo(codigo) != null;
    }

    // Devolve o municipio do codigo, ou null se o codigo nao existe
    public static MunicipioAtendido buscarPorCodigo(String codigo) {
        if (codigo == null)
            return null;

        String procurado = codigo.trim();
        for (MunicipioAtendido municipio : MunicipioAtendido.values()) {
            if (municipio.codigo.equalsIgnoreCase(procurado))
                return municipio;
        }
        return null;
    }
}
