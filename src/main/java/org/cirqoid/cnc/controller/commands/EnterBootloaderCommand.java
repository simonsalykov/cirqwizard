package org.cirqoid.cnc.controller.commands;

/**
 * Created by simon on 29.06.17.
 */
public class EnterBootloaderCommand extends Command
{
    @Override
    public Type getType()
    {
        return Type.ENTER_BOOTLOADER;
    }

    @Override
    public byte[] getPayload()
    {
        return new byte[0];
    }

    @Override
    public String toString()
    {
        return "EnterBootloaderCommand{id = " + getId() + "}";
    }
}
