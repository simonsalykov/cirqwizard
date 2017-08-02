package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.interpreter.Token;

/**
 * Created by simon on 17.06.17.
 */
public class CommandFactory
{
    private static Command[] commands = new Command[] {new G0Command(), new G1Command(), new G2Command(), new G3Command(),
        new G53Command(), new G54Command(), new G92Command(), new M3Command(), new M5Command(),
            new M7Command(), new M8Command(), new M9Command(), new G4Command(), new G28Command()
    };

    public static Command findCommand(Token token)
    {
        for (Command c : commands)
            if (c.getLetter() == token.getLetter() && c.getCode() == token.getIntegerParameter())
                return c;
        return null;
    }
}
