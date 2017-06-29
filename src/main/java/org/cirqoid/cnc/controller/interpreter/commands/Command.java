package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.List;

/**
 * Created by simon on 17.06.17.
 */
public interface Command
{
    char getLetter();
    int getCode();
    List<org.cirqoid.cnc.controller.commands.Command> act(Context context, List<Token> tokens) throws ParsingException;
}
