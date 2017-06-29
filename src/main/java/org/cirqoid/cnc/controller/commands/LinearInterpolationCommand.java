package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by simon on 19.06.17.
 */
public class LinearInterpolationCommand extends Command
{
    private int start[];
    private int target[];
    private int maxExitSpeed;
    private int feed;

    public LinearInterpolationCommand(int[] start, int[] target, int feed)
    {
        this.start = start;
        this.target = target;
        this.feed = feed;
    }

    public int getMaxExitSpeed()
    {
        return maxExitSpeed;
    }

    public void setMaxExitSpeed(int maxExitSpeed)
    {
        this.maxExitSpeed = maxExitSpeed;
    }

    @Override
    public Type getType()
    {
        return Type.LINEAR_INTERPOLATION;
    }

    public int[] getStart()
    {
        return start;
    }

    public int[] getTarget()
    {
        return target;
    }

    public int getFeed()
    {
        return feed;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4 * ApplicationConstants.MAX_AXES_COUNT + 4 + 4);
        for (int i : target)
            b.putInt(i);
        b.putInt(feed);
        b.putInt(maxExitSpeed);
        return b.array();
    }

    @Override
    public String toString()
    {
        return "LinearInterpolationCommand{" +
                "id=" + getId() +
                ", target=" + Arrays.toString(target) +
                ", feed=" + feed +
                ", maxExitSpeed=" + maxExitSpeed +
                '}';
    }
}
