package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.RelayControlCommand;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.Arrays;
import java.util.List;

/**
 * Created by simon on 28.06.17.
 */
public class M9Command implements Command
{
    @Override
    public char getLetter()
    {
        return 'M';
    }

    @Override
    public int getCode()
    {
        return 9;
    }

    @Override
    public List<org.cirqoid.cnc.controller.commands.Command> act(Context context, List<Token> tokens) throws ParsingException
    {
        context.resetRelay(0);
        context.resetRelay(1);
        return Arrays.asList(new RelayControlCommand(context.getRelaysStatus()));
    }
}
