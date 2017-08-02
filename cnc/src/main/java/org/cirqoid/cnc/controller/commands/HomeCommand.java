package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by simon on 16.06.17.
 */
public class HomeCommand extends Command
{
    private int parameters[];

    public HomeCommand(int[] parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public Type getType()
    {
        return Type.HOME;
    }

    public int[] getParameters()
    {
        return parameters;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(ApplicationConstants.MAX_AXES_COUNT * 4);
        for (int i : parameters)
            b.putInt(i);
        return b.array();
    }

    @Override
    public String toString()
    {
        return "HomeCommand{" +
                "parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
