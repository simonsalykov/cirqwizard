package org.cirqwizard.settings;

/**
 * Created by simon on 05/08/14.
 */
public class ApplicationConstants
{
    public static final int RESOLUTION = 1000;
    public static final int ROUNDING = 30;
    private final static int X_RAPIDS = 1_500_000;
    private final static int Y_RAPIDS = 1_000_000;
    private final static int Z_RAPIDS = 1_000_000;
    private final static int X_RAPID_ACCELERATION = 100_000;
    private final static int Y_RAPID_ACCELERATION = 50_000;
    private final static int Z_RAPID_ACCELERATION = 50_000;
    private final static int FEED_ACCELERATION = 50_000;
    private final static int ARC_FEED = 400_000;

    public static int getXRapids()
    {
        return X_RAPIDS;
    }

    public static int getYRapids()
    {
        return Y_RAPIDS;
    }

    public static int getZRapids()
    {
        return Z_RAPIDS;
    }

    public static int getXRapidAcceleration()
    {
        return X_RAPID_ACCELERATION;
    }

    public static int getYRapidAcceleration()
    {
        return Y_RAPID_ACCELERATION;
    }

    public static int getZRapidAcceleration()
    {
        return Z_RAPID_ACCELERATION;
    }

    public static int getFeedAcceleration()
    {
        return FEED_ACCELERATION;
    }

    public static int getArcFeed()
    {
        return ARC_FEED;
    }
}
