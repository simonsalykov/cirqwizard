package org.cirqwizard.render;

import org.cirqwizard.geom.Point;

/**
 * Created by simon on 11/12/13.
 */
public class MatchedArc
{
    private Point center;
    private int radius;
    private double uncertainty;

    public MatchedArc(Point center, int radius, double uncertainty)
    {
        this.center = center;
        this.radius = radius;
        this.uncertainty = uncertainty;
    }

    public Point getCenter()
    {
        return center;
    }

    public void setCenter(Point center)
    {
        this.center = center;
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }

    public double getUncertainty()
    {
        return uncertainty;
    }

    public void setUncertainty(double uncertainty)
    {
        this.uncertainty = uncertainty;
    }
}
