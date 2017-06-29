package org.cirqoid.cnc.controller.interpreter;

import java.math.BigDecimal;

/**
 * Created by simon on 16.06.17.
 */
public class Token
{
    private char letter;
    private int integerParameter;
    private BigDecimal decimalParameter;

    public Token(char letter, int integerParameter)
    {
        this.letter = letter;
        this.integerParameter = integerParameter;
    }

    public Token(char letter, BigDecimal decimalParameter)
    {
        this.letter = letter;
        this.decimalParameter = decimalParameter;
    }

    public char getLetter()
    {
        return letter;
    }

    public int getIntegerParameter()
    {
        return integerParameter;
    }

    public BigDecimal getDecimalParameter()
    {
        return decimalParameter;
    }

    @Override
    public String toString()
    {
        return "Token{" +
                "letter=" + letter +
                ", integerParameter=" + integerParameter +
                ", decimalParameter=" + decimalParameter +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (letter != token.letter) return false;
        if (integerParameter != token.integerParameter) return false;
        return decimalParameter != null ? decimalParameter.equals(token.decimalParameter) : token.decimalParameter == null;
    }

    @Override
    public int hashCode()
    {
        int result = (int) letter;
        result = 31 * result + integerParameter;
        result = 31 * result + (decimalParameter != null ? decimalParameter.hashCode() : 0);
        return result;
    }
}
