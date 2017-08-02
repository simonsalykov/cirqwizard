package org.cirqoid.cnc.analyzer;

/**
 * Created by simon on 21.06.17.
 */
public class StepperAnalyzer
{
    private int counter;
    private int stepBit;
    private int directionBit;
    private boolean lastState;

    public StepperAnalyzer(int stepBit, int directionBit)
    {
        this.stepBit = stepBit;
        this.directionBit = directionBit;
    }

    public void tick(int value)
    {
        boolean currentState = (value & stepBit) != 0;
        boolean direction = (value & directionBit) != 0;
        if (!lastState && currentState)
            counter += direction ? 1 : -1;
        lastState = currentState;
    }

    public double getCounter()
    {
        return -counter * 0.00125;
    }
}
