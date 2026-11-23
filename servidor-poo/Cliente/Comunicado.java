/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descricao: Classe base serializavel para comunicacao entre Cliente e Servidor via Sockets TCP
*/

import java.io.Serializable;

/**
 * Superclasse de todos os pedidos e respostas trocados entre Cliente e Servidor.
 * O serialVersionUID fixo garante compatibilidade na desserializacao entre os dois lados.
 */
public class Comunicado implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;
}
