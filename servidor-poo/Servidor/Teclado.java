/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Utilitario de leitura de dados pelo teclado no padrao da disciplina POO/Java
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Centraliza a leitura tipada da entrada padrao (System.in) lancando excecao em formatos invalidos.
 */
public class Teclado
{
    private static BufferedReader teclado =
        new BufferedReader(new InputStreamReader(System.in));

    // Le uma linha completa da entrada padrao (retorna null se o stream fechar)
    public static String getUmString ()
    {
        String ret = null;

        try
        {
            ret = teclado.readLine();
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro

        return ret;
    }

    public static byte getUmByte () throws Exception
    {
        byte ret = (byte) 0;

        try
        {
            ret = Byte.parseByte(teclado.readLine());
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro
        catch (NumberFormatException erro)
        {
            throw new Exception("Byte invalido!");
        }

        return ret;
    }

    public static short getUmShort () throws Exception
    {
        short ret = (short) 0;

        try
        {
            ret = Short.parseShort(teclado.readLine());
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro
        catch (NumberFormatException erro)
        {
            throw new Exception("Short invalido!");
        }

        return ret;
    }

    // Converte a linha lida para inteiro ou lanca excecao amigavel
    public static int getUmInt () throws Exception
    {
        int ret = 0;

        try
        {
            ret = Integer.parseInt(teclado.readLine());
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro
        catch (NumberFormatException erro)
        {
            throw new Exception("Int invalido!");
        }

        return ret;
    }

    public static long getUmLong () throws Exception
    {
        long ret = 0L;

        try
        {
            ret = Long.parseLong(teclado.readLine());
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro
        catch (NumberFormatException erro)
        {
            throw new Exception("Long invalido!");
        }

        return ret;
    }

    public static float getUmFloat () throws Exception
    {
        float ret = 0.0F;

        try
        {
            ret = Float.parseFloat(teclado.readLine());
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro
        catch (NumberFormatException erro)
        {
            throw new Exception("Float invalido!");
        }

        return ret;
    }

    // Converte a linha lida para numero real (double) ou lanca excecao amigavel
    public static double getUmDouble () throws Exception
    {
        double ret = 0.0;

        try
        {
            ret = Double.parseDouble(teclado.readLine());
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro
        catch (NumberFormatException erro)
        {
            throw new Exception("Double invalido!");
        }

        return ret;
    }

    // Exige exatamente 1 caractere digitado
    public static char getUmChar () throws Exception
    {
        char ret = ' ';

        try
        {
            String str = teclado.readLine();

            if (str == null)
                throw new Exception("Char invalido!");

            if (str.length() != 1)
                throw new Exception("Char invalido!");

            ret = str.charAt(0);
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro

        return ret;
    }

    public static boolean getUmBoolean () throws Exception
    {
        boolean ret = false;

        try
        {
            String str = teclado.readLine();

            if (str == null)
                throw new Exception("Boolean invalido!");

            if (!str.equals("true") && !str.equals("false"))
                throw new Exception("Boolean invalido!");

            ret = Boolean.parseBoolean(str);
        }
        catch (IOException erro)
        {} // sei que nao vai dar erro

        return ret;
    }
}
