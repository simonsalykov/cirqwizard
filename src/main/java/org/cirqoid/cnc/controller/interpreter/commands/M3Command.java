package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.SpindleControlCommand;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.Arrays;
import java.util.List;

/**
 * Created by simon on 28.06.17.
 */
public class M3Command implements Command
{
    @Override
    public char getLetter()
    {
        return 'M';
    }

    @Override
    public int getCode()
    {
        return 3;
    }

    @Override
    public List<org.cirqoid.cnc.controller.commands.Command> act(Context context, List<Token> tokens) throws ParsingException
    {
        if (context.getSpeed() == null)
            throw new ParsingException("Spindle speed is not set");
        return Arrays.asList(new SpindleControlCommand(context.getSpeed()));
    }
}
