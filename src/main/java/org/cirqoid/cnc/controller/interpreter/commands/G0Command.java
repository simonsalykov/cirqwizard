package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.List;

/**
 * Created by simon on 17.06.17.
 */
public class G0Command implements org.cirqoid.cnc.controller.interpreter.commands.Command
{
    @Override
    public char getLetter()
    {
        return 'G';
    }

    @Override
    public int getCode()
    {
        return 0;
    }

    @Override
    public List<Command> act(Context context, List<Token> tokens)
    {
        context.setCurrentInterpolationMode(Context.InterpolationMode.RAPID);
        return null;
    }
}
