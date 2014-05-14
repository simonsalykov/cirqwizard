package org.cirqwizard.generation;

import org.cirqwizard.geom.Circle;

/**
 * Created by simon on 11/12/13.
 */
public class MatchedArc
{
    private Circle circle;
    private double uncertainty;

    public MatchedArc(Circle circle, double uncertainty)
    {
        this.circle = circle;
        this.uncertainty = uncertainty;
    }

    public Circle getCircle()
    {
        return circle;
    }

    public void setCircle(Circle circle)
    {
        this.circle = circle;
    }

    public double getUncertainty()
    {
        return uncertainty;
    }

    public void setUncertainty(double uncertainty)
    {
        this.uncertainty = uncertainty;
    }

    @Override
    public String toString()
    {
        return "MatchedArc{" +
                "circle=" + circle +
                ", uncertainty=" + uncertainty +
                '}';
    }
}
