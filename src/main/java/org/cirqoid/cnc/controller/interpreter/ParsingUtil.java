package org.cirqoid.cnc.controller.interpreter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by simon on 16.06.17.
 */
public class ParsingUtil
{
    private static final BigDecimal RESOLUTION = new BigDecimal(1000);

    public static boolean listContainsToken(List<Token> tokens, char letter)
    {
        return tokens.stream().anyMatch(t -> t.getLetter() == letter);
    }

    public static boolean listContainsToken(List<Token> tokens, char letter, int parameter)
    {
        return tokens.stream().anyMatch(t -> t.getLetter() == letter && t.getIntegerParameter() == parameter);
    }

    public static boolean isCommandLetter(char letter)
    {
        return letter == 'G' || letter == 'M';
    }

    public static boolean isAxisLetter(char letter)
    {
        return letter == 'X' || letter == 'Y' || letter == 'Z' || letter == 'A';
    }

    public static boolean isCenterOffsetLetter(char letter)
    {
        return letter == 'I' || letter == 'J' || letter == 'K';
    }

    public static boolean isDimensionLetter(char letter)
    {
        return isAxisLetter(letter) || isCenterOffsetLetter(letter) || letter == 'F' || letter == 'P';
    }

    public static int toInteger(BigDecimal bd)
    {
        return bd.multiply(RESOLUTION).intValue();
    }

    public static int getAxisNumber(char axis) throws ParsingException
    {
        switch (axis)
        {
            case 'I': return 0;
            case 'J': return 1;
            case 'K': return 2;
            case 'X': return 0;
            case 'Y': return 1;
            case 'Z': return 2;
            case 'A': return 3;
        }
        throw new ParsingException("Unknown axis: " + axis);
    }

}
