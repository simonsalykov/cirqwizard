package org.cirqwizard.gerber.appertures.macro;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.cirqwizard.generation.VectorToolPathGenerator;
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

    @Override
    public Polygon createPolygon(int x, int y, int inflation)
    {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < getPoints().size(); i++)
        {
            Point point = getPoints().get(i);
            coordinates.add(new Coordinate(point.getX() + x, point.getY() + y));
        }
        coordinates.add(coordinates.get(0));

        Coordinate[] c = new Coordinate[coordinates.size()];
        coordinates.toArray(c);
        return (Polygon) VectorToolPathGenerator.factory.createPolygon(c).buffer(inflation);
    }
}
