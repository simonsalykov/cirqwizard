package org.cirqwizard.render;

/**
 * Created by simon on 11/12/13.
 */
public class MatchedArc
{
    private PointI center;
    private double radius;
    private double uncertainty;

    public MatchedArc(PointI center, double radius, double uncertainty)
    {
        this.center = center;
        this.radius = radius;
        this.uncertainty = uncertainty;
    }

    public PointI getCenter()
    {
        return center;
    }

    public void setCenter(PointI center)
    {
        this.center = center;
    }

    public double getRadius()
    {
        return radius;
    }

    public void setRadius(double radius)
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
