package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

/**
 * Created by simon on 27.06.17.
 */
public class SpindleControlCommand extends Command
{
    private int speed;

    public SpindleControlCommand(int speed)
    {
        this.speed = speed;
    }

    @Override
    public Type getType()
    {
        return Type.SPINDLE_CONTROL;
    }

    public int getSpeed()
    {
        return speed;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(speed);
        return b.array();
    }
}
