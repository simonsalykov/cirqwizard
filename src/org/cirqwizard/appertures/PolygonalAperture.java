package org.cirqwizard.appertures;

import org.cirqwizard.geom.Point;

import java.util.ArrayList;

public class PolygonalAperture extends Aperture
{
    private ArrayList<Point> points = new ArrayList<Point>();
    private int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;

    public PolygonalAperture(ArrayList<Point> points)
    {
        super();
        this.points = points;

        for (Point p : points)
        {
            maxX = Math.max(maxX, p.getX());
            minX = Math.min(minX, p.getX());
        }
    }

    public PolygonalAperture(int diameter, int holeDiameter)
    {
        super(holeDiameter);
    }

    public PolygonalAperture(int diameter, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
    }

    public ArrayList<Point> getPoints()
    {
        return points;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        ArrayList<Point> newPoints = new ArrayList<Point>();
        for(Point p : points)
        {
            Point point;
            if (clockwise)
                point = new Point(p.getY(), -p.getX());
            else
                point = new Point(-p.getY(), p.getX());
            newPoints.add(point);
        }
        points = newPoints;
        return this;
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public int getWidth(double angle)
    {
        return maxX - minX;
    }
}
