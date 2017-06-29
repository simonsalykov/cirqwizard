package org.cirqoid.cnc.controller.interpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 16.06.17.
 */
public class GCodeParser
{
    private static Token formToken(char letter, String parameter) throws ParsingException
    {
        try
        {
            if (ParsingUtil.isCommandLetter(letter) || letter == 'S')
                return new Token(letter, Integer.valueOf(parameter));
            else
                return new Token(letter, new BigDecimal(parameter));
        }
        catch (NumberFormatException e)
        {
            throw new ParsingException("Incorrect parameter for command " + letter + ": " + parameter);
        }
    }

    private static void addToken(List<Token> tokens, Character currentLetter, String parameter) throws ParsingException
    {
        if (currentLetter == null)
            return;
        if (ParsingUtil.isDimensionLetter(currentLetter) && ParsingUtil.listContainsToken(tokens, currentLetter))
            throw new ParsingException("Dimension " + parameter + " is already defined");
        tokens.add(formToken(currentLetter, parameter));
    }

    public static List<Token> parseBlock(String block) throws ParsingException
    {
        block = block.replaceAll("\\s", "");
        block = block.replaceAll("\\(.*\\)", "");
        block = block.toUpperCase();
        Character currentLetter = null;
        StringBuffer parameter = new StringBuffer();
        List<Token> tokens = new ArrayList<>();
        for (char c : block.toCharArray())
        {
            if (ParsingUtil.isCommandLetter(c) || ParsingUtil.isDimensionLetter(c) || c == 'S')
            {
                addToken(tokens, currentLetter, parameter.toString());
                currentLetter = c;
                parameter = new StringBuffer();
            }
            else
            {
                if (currentLetter != null && (Character.isDigit(c) ||
                        (c == '-' && parameter.length() == 0) ||
                        (c == '.' && ParsingUtil.isDimensionLetter(currentLetter))))
                    parameter.append(c);
                else
                    throw new ParsingException("Unexpected symbol: " + c);
            }
        }
        addToken(tokens, currentLetter, parameter.toString());
        return tokens;
    }

}
