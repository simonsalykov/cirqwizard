package org.cirqwizard.gerber.appertures.macro;

import org.cirqwizard.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class MacroPolygon extends MacroPrimitive
{
    private int verticesCount;
    private Point center;
    private int diameter;

    public MacroPolygon(int verticesCount, Point center, int diameter, int rotationAngle)
    {
        super(rotationAngle);
        this.verticesCount = verticesCount;
        this.center = center;
        this.diameter = diameter;
    }

    public int getVerticesCount()
    {
        return verticesCount;
    }

    public Point getCenter()
    {
        return center;
    }

    public int getDiameter()
    {
        return diameter;
    }

    public List<Point> getPoints()
    {
        double angle = getRotationAngle();
        int angularIncrement = 360 / verticesCount;
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < verticesCount; i++)
        {
            double theta = angle / 180 * Math.PI;
            Point p = center.add(new Point(diameter, 0));
            points.add(new Point((int)(Math.cos(theta) * p.getX() - Math.sin(theta) * p.getY()),
                    (int)(Math.sin(theta) * p.getX() + Math.cos(theta) * p.getY())));
            angle += angularIncrement;
        }
        return points;
    }

    @Override
    public MacroPrimitive clone()
    {
        return new MacroPolygon(verticesCount, new Point(center.getX(), center.getY()),
                diameter, getRotationAngle());
    }
}
