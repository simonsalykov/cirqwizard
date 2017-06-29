package org.cirqoid.cnc.controller.settings;

/**
 * Created by simon on 28.06.17.
 */
public class Axis
{
    private int lowLimit;
    private int highLimit;

    public Axis(int lowLimit, int highLimit)
    {
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    public int getLowLimit()
    {
        return lowLimit;
    }

    public int getHighLimit()
    {
        return highLimit;
    }
}
