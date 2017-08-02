package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by simon on 19.06.17.
 */
public class CircularInterpolationCommand extends Command
{

    private int[] positions = new int[ApplicationConstants.MAX_AXES_COUNT];
    private int radius;
    private int centerCoordinates[] = new int[2];
    private Context.Plane plane;
    private boolean clockwise;
    private int feed;

    public CircularInterpolationCommand(int[] positions, int radius, int[] centerCoordinates, Context.Plane plane, boolean clockwise, int feed)
    {
        this.positions = positions;
        this.radius = radius;
        this.centerCoordinates = centerCoordinates;
        this.plane = plane;
        this.clockwise = clockwise;
        this.feed = feed;
    }

    @Override
    public Type getType()
    {
        return Type.CIRCULAR_INTERPOLATION;
    }

    public int[] getPositions()
    {
        return positions;
    }

    public int getRadius()
    {
        return radius;
    }

    public int[] getCenterCoordinates()
    {
        return centerCoordinates;
    }

    public Context.Plane getPlane()
    {
        return plane;
    }

    public boolean isClockwise()
    {
        return clockwise;
    }

    public int getFeed()
    {
        return feed;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4 * ApplicationConstants.MAX_AXES_COUNT + 2 * 4 + 4 + 4 + 4 + 4);
        for (int i : positions)
            b.putInt(i);
        b.putInt(radius);
        for (int i : centerCoordinates)
            b.putInt(i);
        b.putInt(plane.i);
        b.putInt(clockwise ? 1 : 0);
        b.putInt(feed);
        return b.array();

    }

    @Override
    public String toString()
    {
        return "CircularInterpolationCommand{" +
                "positions=" + Arrays.toString(positions) +
                ", radius=" + radius +
                ", centerCoordinates=" + Arrays.toString(centerCoordinates) +
                ", plane=" + plane +
                ", clockwise=" + clockwise +
                ", feed=" + feed +
                '}';
    }
}
