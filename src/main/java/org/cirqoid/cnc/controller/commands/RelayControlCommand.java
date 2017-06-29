package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

/**
 * Created by simon on 27.06.17.
 */
public class RelayControlCommand extends Command
{
    private int status;

    public RelayControlCommand(int status)
    {
        this.status = status;
    }

    @Override
    public Type getType()
    {
        return Type.RELAY_CONTROL;
    }

    public int getStatus()
    {
        return status;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(status);
        return b.array();
    }
}
