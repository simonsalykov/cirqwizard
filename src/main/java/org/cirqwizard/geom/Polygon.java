package org.cirqwizard.geom;

import org.cirqwizard.math.MathUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Polygon implements Serializable
{
    private List<Point> vertices;

    public Polygon()
    {
        vertices = new ArrayList<>();
    }

    public Polygon(List<Point> vertices)
    {
        this.vertices = vertices;
    }

    public void addVertice(Point point)
    {
        vertices.add(point);
    }

    public Polygon transform(Point point)
    {
        Polygon transformedPolygon = new Polygon();
        for (Point vertice : vertices)
        {
            transformedPolygon.addVertice(vertice.add(point));
        }
        return transformedPolygon;
    }

    public Line getLongestEdge()
    {
        int longestLineIndex = 0;
        double longestDistance = vertices.get(longestLineIndex).distanceTo(vertices.get(longestLineIndex + 1));

        for (int i = 0; i < vertices.size() - 1; ++i)
        {
            double distance = vertices.get(i).distanceTo(vertices.get((i + 1)));
            if (distance > longestDistance)
            {
                longestDistance = distance;
                longestLineIndex = i;
            }
        }

        return new Line(vertices.get(longestLineIndex), vertices.get(longestLineIndex + 1));
    }

    public boolean lineBelongsToPolygon(Line line)
    {
        Point from = line.getFrom();
        Point to = line.getTo();

        Point midPoint = new Point((from.getX() + to.getX()) / 2, (to.getY() + from.getY()) / 2);
        return pointBelongsToPolygon(midPoint) || pointBelongsToPolygon(from) || pointBelongsToPolygon(to);
    }

    private boolean pointBetweenVertices(Point point)
    {
        // improve performance
        if (!vertices.stream().anyMatch(p -> p.getX() <= point.getX()))
            return false;

        if (!vertices.stream().anyMatch(p -> p.getX() >= point.getX()))
            return false;

        // improve performance
        if (!vertices.stream().anyMatch(p -> p.getY() <= point.getY()))
            return false;

        if (!vertices.stream().anyMatch(p -> p.getY() >= point.getY()))
            return false;

        return true;
    }
    // https://www.geeksforgeeks.org/how-to-check-if-a-given-point-lies-inside-a-polygon/
    // This algorith is not precise, it works only when points are located between the vertices.
    // improve it by checking the point before passing to this algorithm
    public boolean pointBelongsToPolygon(Point point)
    {
        if (!pointBetweenVertices(point))
        {
            return false;
        }

        int biggestX = vertices.stream().max(Comparator.comparingInt(Point::getX)).get().getX() + 1;
        Point infPoint = new Point(biggestX, point.getY());

        int count = 0, i = 0;
        do
        {
            int next = (i + 1) % vertices.size();

            if (MathUtil.segmentsIntersect(vertices.get(i), vertices.get(next), point, infPoint))
            {
                if (MathUtil.getOrientation(vertices.get(i), vertices.get(next), point) == 0)
                    return MathUtil.pointOnLine(vertices.get(i), vertices.get(next), point);

                // if the line crosses the vertice, we don't count it twice
                if (vertices.get(i).getY() == point.getY())
                    count -= 1;

                count += 1;
            }
            i = next;
        }
        while (i != 0);
        return count % 2 == 1;
    }
}
