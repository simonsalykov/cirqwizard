package org.cirqoid.cnc.controller.interpreter.commands;

import org.cirqoid.cnc.controller.commands.SleepCommand;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.interpreter.ParsingUtil;
import org.cirqoid.cnc.controller.interpreter.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by simon on 28.06.17.
 */
public class G4Command implements Command
{
    @Override
    public char getLetter()
    {
        return 'G';
    }

    @Override
    public int getCode()
    {
        return 4;
    }

    @Override
    public List<org.cirqoid.cnc.controller.commands.Command> act(Context context, List<Token> tokens) throws ParsingException
    {
        Optional<Token> token = tokens.stream().filter(t -> t.getLetter() == 'P').findFirst();
        if (!token.isPresent())
            throw new ParsingException("P parameter missing");
        tokens.remove(token.get());
        return Arrays.asList(new SleepCommand(ParsingUtil.toInteger(token.get().getDecimalParameter())));
    }
}
