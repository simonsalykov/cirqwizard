package org.cirqoid.cnc.controller.interpreter;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by simon on 16.06.17.
 */
public class ParserTest
{

    @Test
    public void testValidCommand() throws ParsingException
    {
        List<Token> tokens = GCodeParser.parseBlock("G0 X10 Y2.4 F500");
        assertEquals(4, tokens.size());
        assertEquals('G', tokens.get(0).getLetter());
        assertEquals(0, tokens.get(0).getIntegerParameter());
        assertEquals('X', tokens.get(1).getLetter());
        assertEquals(new BigDecimal(10), tokens.get(1).getDecimalParameter());
        assertEquals('Y', tokens.get(2).getLetter());
        assertEquals(new BigDecimal("2.4"), tokens.get(2).getDecimalParameter());
        assertEquals('F', tokens.get(3).getLetter());
        assertEquals(new BigDecimal("500"), tokens.get(3).getDecimalParameter());
    }

    @Test
    public void testInvalidLetter()
    {
        try
        {
            GCodeParser.parseBlock("R10 X20");
            fail("ParsingException should have been thrown");
        }
        catch (ParsingException e) {}
    }

    @Test
    public void testComment() throws ParsingException
    {
        List<Token> tokens = GCodeParser.parseBlock("G1 X4.3 (why so?) Y44.11Z8");
        assertEquals(4, tokens.size());
        assertEquals('G', tokens.get(0).getLetter());
        assertEquals(1, tokens.get(0).getIntegerParameter());
        assertEquals('X', tokens.get(1).getLetter());
        assertEquals(new BigDecimal("4.3"), tokens.get(1).getDecimalParameter());
        assertEquals('Y', tokens.get(2).getLetter());
        assertEquals(new BigDecimal("44.11"), tokens.get(2).getDecimalParameter());
        assertEquals('Z', tokens.get(3).getLetter());
        assertEquals(new BigDecimal("8"), tokens.get(3).getDecimalParameter());
    }

    @Test
    public void testDoubleCommand() throws ParsingException
    {
        List<Token> tokens = GCodeParser.parseBlock("G54 G0 X10.");
        assertEquals(3, tokens.size());
    }

    @Test
    public void testDoubleDimension()
    {
        try
        {
            GCodeParser.parseBlock("G0 X10 X20");
            fail("ParsingException should have been thrown");
        }
        catch (ParsingException e) {}
    }

    @Test
    public void testSpindleSpeed() throws ParsingException
    {
        List<Token> tokens = GCodeParser.parseBlock("S100");
        assertEquals(1, tokens.size());
        assertEquals('S', tokens.get(0).getLetter());
        assertEquals(100, tokens.get(0).getIntegerParameter());
    }

    @Test
    public void testDecimalSpindleSpeed()
    {
        try
        {
            GCodeParser.parseBlock("S100.5");
            fail("ParsingException should have been thrown");
        }
        catch (ParsingException e) {}
    }



}
