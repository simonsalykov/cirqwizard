package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.List;

/**
 * Created by simon on 19.06.17.
 */
public class G2Command implements org.cirqoid.cnc.controller.interpreter.commands.Command
{
    @Override
    public char getLetter()
    {
        return 'G';
    }

    @Override
    public int getCode()
    {
        return 2;
    }

    @Override
    public List<Command> act(Context interpreter, List<Token> tokens)
    {
        interpreter.setCurrentInterpolationMode(Context.InterpolationMode.CIRCUAR_CW);
        return null;
    }
}
