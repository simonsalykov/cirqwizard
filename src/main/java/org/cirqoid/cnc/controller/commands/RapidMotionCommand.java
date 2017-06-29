package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by simon on 14.06.17.
 */
public class RapidMotionCommand extends Command
{
    private int[] positions;

    public RapidMotionCommand(int[] positions)
    {
        this.positions = positions;
    }

    @Override
    public Type getType()
    {
        return Type.RAPID_MOTION;
    }

    public int[] getPositions()
    {
        return positions;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4 * ApplicationConstants.MAX_AXES_COUNT);
        for (int i : positions)
            b.putInt(i);
        return b.array();
    }

    @Override
    public String toString()
    {
        return "RapidMotionCommand{" +
                "positions=" + Arrays.toString(positions) +
                '}';
    }
}
