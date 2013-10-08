package org.cirqwizard.appertures;

import org.cirqwizard.math.RealNumber;

import org.cirqwizard.geom.Point;

import java.awt.Graphics2D;
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
            if ((int) (p.getX().doubleValue()) > maxX)
                maxX = (int) (p.getX().doubleValue());

            if ((int) (p.getX().doubleValue()) < minX)
                minX = (int) (p.getX().doubleValue());
        }
    }

    public PolygonalAperture(RealNumber diameter, RealNumber holeDiameter)
    {
        super(holeDiameter);
    }

    public PolygonalAperture(RealNumber diameter, RealNumber holeWidth, RealNumber holeHeight)
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
                point = new Point(p.getY(), p.getX().negate());
            else
                point = new Point(p.getY().negate(), p.getX());
            newPoints.add(point);
        }
        points = newPoints;
        return this;
    }

    @Override
    public void render(Graphics2D g, int x, int y, double scale)
    {
        int xPoints[] = new int[points.size()];
        int yPoints[] = new int[points.size()];
        int idx = 0;

        for (Point p : points)
        {
            xPoints[idx] = (int) (p.getX().doubleValue() * scale) + x;
            yPoints[idx] = (int) (p.getX().doubleValue() * scale) + x;
            idx++;
        }
        g.fillPolygon(xPoints, yPoints, points.size());
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public RealNumber getWidth(RealNumber angle)
    {
        return new RealNumber(maxX - minX);
    }
}
