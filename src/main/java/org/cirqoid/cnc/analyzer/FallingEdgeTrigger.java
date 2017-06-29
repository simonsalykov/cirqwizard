package org.cirqoid.cnc.analyzer;

/**
 * Created by simon on 21.06.17.
 */
public class FallingEdgeTrigger
{
    private int bit;
    private boolean lastState;

    public FallingEdgeTrigger(int bit)
    {
        this.bit = bit;
    }

    public boolean tick(int value)
    {
        boolean currentValue = (value & bit) != 0;
        boolean trigger = lastState && !currentValue;
        lastState = currentValue;
        return trigger;
    }
}
