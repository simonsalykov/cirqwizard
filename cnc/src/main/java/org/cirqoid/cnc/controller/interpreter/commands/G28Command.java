package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.HomeCommand;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.interpreter.ParsingUtil;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by simon on 28.06.17.
 */
public class G28Command implements Command
{
    @Override
    public char getLetter()
    {
        return 'G';
    }

    @Override
    public int getCode()
    {
        return 28;
    }

    @Override
    public List<org.cirqoid.cnc.controller.commands.Command> act(Context context, List<Token> tokens) throws ParsingException
    {
        int[] parameters = new int[3];
        List<Token> toRemove = new ArrayList<>();
        tokens.forEach(t ->
        {
            try
            {
                if (ParsingUtil.isAxisLetter(t.getLetter()))
                {
                    parameters[ParsingUtil.getAxisNumber(t.getLetter())] = ParsingUtil.toInteger(t.getDecimalParameter());
                    toRemove.add(t);
                }
            }
            catch (ParsingException e) {}
        });
        tokens.removeAll(toRemove);
        return Arrays.asList(new HomeCommand(parameters));
    }
}
