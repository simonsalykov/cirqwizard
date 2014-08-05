package org.cirqwizard.settings;

/**
 * Created by simon on 05/08/14.
 */
public enum  DistanceUnit
{
    INCHES(25_400, "Inches"), MM(1000, "Millimeters");

    private int multiplier;
    private String name;

    DistanceUnit(int multiplier, String name)
    {
        this.multiplier = multiplier;
        this.name = name;
    }

    public int getMultiplier()
    {
        return multiplier;
    }

    public String getName()
    {
        return name;
    }

}
