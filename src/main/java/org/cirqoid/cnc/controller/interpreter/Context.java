package org.cirqoid.cnc.controller.interpreter;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

/**
 * Created by simon on 17.06.17.
 */
public class Context implements Cloneable
{
    public enum Plane
    {
        XY(1), YZ(2), XZ(3);

        public int i;

        Plane(int i)
        {
            this.i = i;
        }
    }
    public enum InterpolationMode
    {
        NOT_SELECTED, RAPID, LINEAR, CIRCUAR_CW, CIRCULAR_CCW
    }

    private InterpolationMode currentInterpolationMode = Context.InterpolationMode.NOT_SELECTED;
    private int currentWcs = 0;
    private int offsets[][] = new int[ApplicationConstants.WCS_COUNT][ApplicationConstants.MAX_AXES_COUNT + 1];
    private int currentPosition[] = new int[ApplicationConstants.MAX_AXES_COUNT];
    private Plane plane = Plane.XY;
    private int arcCenterOffset[] = new int[3];
    private Integer feed = null;
    private Integer speed = null;
    private int relaysStatus;

    public InterpolationMode getCurrentInterpolationMode()
    {
        return currentInterpolationMode;
    }

    public void setCurrentInterpolationMode(InterpolationMode currentInterpolationMode)
    {
        this.currentInterpolationMode = currentInterpolationMode;
    }

    public int getCurrentWcs()
    {
        return currentWcs;
    }

    public void setCurrentWcs(int currentWcs)
    {
        this.currentWcs = currentWcs;
    }

    public int getOffset(int wcs, int axis)
    {
        return offsets[wcs][axis];
    }

    public void setOffset(int wcs, int axis, int offset)
    {
        offsets[wcs][axis] = offset;
    }

    public int[] getCurrentPosition()
    {
        return currentPosition;
    }

    public int getCurrentPosition(int axis)
    {
        return currentPosition[axis];
    }

    public void setCurrentPosition(int axis, int position)
    {
        currentPosition[axis] = position;
    }

    public Plane getPlane()
    {
        return plane;
    }

    public void setPlane(Plane plane)
    {
        this.plane = plane;
    }

    public int getArcCenterOffset(int axis)
    {
        return arcCenterOffset[axis];
    }

    public void setArcCenterOffset(int axis, int offset)
    {
        arcCenterOffset[axis] = offset;
    }

    public Integer getFeed()
    {
        return feed;
    }

    public void setFeed(Integer feed)
    {
        this.feed = feed;
    }

    public Integer getSpeed()
    {
        return speed;
    }

    public void setSpeed(Integer speed)
    {
        this.speed = speed;
    }

    public int getRelaysStatus()
    {
        return relaysStatus;
    }

    public void setRelay(int relay)
    {
        relaysStatus |= 1 << relay;
    }

    public void resetRelay(int relay)
    {
        relaysStatus = relaysStatus & ~(1 << relay);
    }

    @Override
    public Object clone()
    {
        Context clone = new Context();
        clone.currentInterpolationMode = this.currentInterpolationMode;
        clone.currentWcs = this.currentWcs;
        for (int wcs = 0; wcs < ApplicationConstants.WCS_COUNT; wcs++)
            for (int axis = 0; axis < ApplicationConstants.MAX_AXES_COUNT; axis++)
                clone.offsets[wcs][axis] = this.offsets[wcs][axis];
        System.arraycopy(this.currentPosition, 0, clone.currentPosition, 0, this.currentPosition.length);;
        clone.feed = this.feed;
        return clone;
    }

}
