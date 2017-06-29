package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

/**
 * Created by simon on 27.06.17.
 */
public class SleepCommand extends Command
{
    private int duration;

    public SleepCommand(int duration)
    {
        this.duration = duration;
    }

    @Override
    public Type getType()
    {
        return Type.SLEEP;
    }

    public int getDuration()
    {
        return duration;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(duration);
        return b.array();
    }
}
