package org.cirqoid.cnc.controller.commands;

/**
 * Created by simon on 23.06.17.
 */
public class RequestVersionCommand extends Command
{
    @Override
    public Type getType()
    {
        return Type.REQUEST_VERSION;
    }

    @Override
    public byte[] getPayload()
    {
        return new byte[0];
    }

    @Override
    public String toString()
    {
        return "RequestVersionCommand{id=" + getId() + "}";
    }
}
