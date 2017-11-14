package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class StatusResponse extends Response
{
    private int runLevel;
    private int[] positions;

    public StatusResponse(int packetId, byte[] payload)
    {
        super(packetId, Code.STATUS);
        ByteBuffer b = ByteBuffer.wrap(payload);
        runLevel = b.getInt();
        positions = new int[ApplicationConstants.MAX_AXES_COUNT];
        for (int i = 0; i < positions.length; i++)
            positions[i] = b.getInt();
    }

    public int getRunLevel()
    {
        return runLevel;
    }

    public int[] getPositions()
    {
        return positions;
    }

    @Override
    public String toString()
    {
        return "StatusResponse{" +
                "runLevel=" + runLevel +
                ", positions=" + Arrays.toString(positions) +
                '}';
    }
}
