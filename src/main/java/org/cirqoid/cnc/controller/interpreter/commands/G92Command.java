package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.interpreter.ParsingUtil;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by simon on 21.06.17.
 */
public class G92Command implements org.cirqoid.cnc.controller.interpreter.commands.Command
{
    @Override
    public char getLetter()
    {
        return 'G';
    }

    @Override
    public int getCode()
    {
        return 92;
    }

    @Override
    public List<Command> act(Context interpreter, List<Token> tokens)
    {
        tokens.forEach(t ->
        {
            try
            {
                if (ParsingUtil.isAxisLetter(t.getLetter()))
                    interpreter.setOffset(1, ParsingUtil.getAxisNumber(t.getLetter()),
                            ParsingUtil.toInteger(t.getDecimalParameter()));
            }
            catch (ParsingException e) {}
        });
        List<Token> toDelete = tokens.stream().filter(t -> ParsingUtil.isAxisLetter(t.getLetter())).collect(Collectors.toList());
        tokens.removeAll(toDelete);
        return null;
    }
}
