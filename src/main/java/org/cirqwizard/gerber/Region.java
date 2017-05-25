/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package org.cirqwizard.gerber;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import javafx.scene.canvas.GraphicsContext;
import org.cirqwizard.generation.VectorToolPathGenerator;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Region extends GerberPrimitive
{
    private List<GerberPrimitive> segments = new ArrayList<>();

    public Region(Polarity polarity)
    {
        super(polarity);
    }

    public void addSegment(GerberPrimitive segment)
    {
        segments.add(segment);
    }

    public List<GerberPrimitive> getSegments()
    {
        return segments;
    }

    @Override
    public void rotate(boolean clockwise)
    {
        for (GerberPrimitive segment : segments)
            segment.rotate(clockwise);
    }

    @Override
    public void move(Point point)
    {
        for (GerberPrimitive segment : segments)
            segment.move(point);
    }

    @Override
    public Point getMin()
    {
        int minX = segments.stream().mapToInt(p -> p.getMin().getX()).min().getAsInt();
        int minY = segments.stream().mapToInt(p -> p.getMin().getY()).min().getAsInt();
        return new Point(minX, minY);
    }

    @Override
    public Point getMax()
    {
        int maxX = segments.stream().mapToInt(p -> p.getMax().getX()).max().getAsInt();
        int maxY = segments.stream().mapToInt(p -> p.getMax().getY()).max().getAsInt();
        return new Point(maxX, maxY);
    }

    @Override
    public void render(Graphics2D g, double inflation)
    {
        Path2D polygon = new GeneralPath();

        Point p = ((InterpolatingShape) getSegments().get(0)).getFrom();
        polygon.moveTo(p.getX(), p.getY());
        for (GerberPrimitive segment : getSegments())
        {
            if (segment instanceof LinearShape)
            {
                LinearShape linearShape = (LinearShape) segment;
                polygon.lineTo(linearShape.getTo().getX(), linearShape.getTo().getY());
            }
            else if (segment instanceof CircularShape)
            {
                Arc arc = ((CircularShape) segment).getArc();
                polygon.append(new Arc2D.Double(arc.getCenter().getX() - arc.getRadius(),
                                arc.getCenter().getY() - arc.getRadius(),
                                arc.getRadius() * 2, arc.getRadius() * 2,
                                -Math.toDegrees(arc.getStart()),
                                Math.toDegrees(arc.getAngle()) * (arc.isClockwise() ? 1 : -1), Arc2D.OPEN),
                        true);
            }
        }
        g.fill(polygon);

        float width = (float) inflation * 2;
        if (width < 0)
        {
            width *= -1;
            g.setColor(getPolarity() == GerberPrimitive.Polarity.DARK ? Color.BLACK : Color.WHITE);
        }
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        for (GerberPrimitive segment : getSegments())
        {
            if (segment instanceof LinearShape)
            {
                LinearShape linearShape = (LinearShape) segment;
                g.draw(new Line2D.Double(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                        linearShape.getTo().getX(), linearShape.getTo().getY()));
            }
            else if (segment instanceof CircularShape)
            {
                Arc arc = ((CircularShape) segment).getArc();
                g.draw(new Arc2D.Double(arc.getCenter().getX() - arc.getRadius(), arc.getCenter().getY() - arc.getRadius(),
                        arc.getRadius() * 2, arc.getRadius() * 2,
                        -Math.toDegrees(arc.getStart()), Math.toDegrees(arc.getAngle()) * (arc.isClockwise() ? 1 : -1), Arc2D.OPEN));
            }
        }

    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public void render(GraphicsContext g)
    {
        g.beginPath();
        InterpolatingShape firstElement = (InterpolatingShape) getSegments().get(0);
        g.moveTo(firstElement.getFrom().getX(), firstElement.getFrom().getY());

        for (GerberPrimitive segment : getSegments())
        {
            if (segment instanceof LinearShape)
                g.lineTo(((LinearShape)segment).getTo().getX(), ((LinearShape)segment).getTo().getY());
            else if (segment instanceof CircularShape)
            {
                Arc arc = ((CircularShape) segment).getArc();
                g.arc(arc.getCenter().getX(), arc.getCenter().getY(),
                        arc.getRadius(), arc.getRadius(),
                        -Math.toDegrees(arc.getStart()), Math.toDegrees(arc.getAngle()) * (arc.isClockwise() ? 1 : -1));
            }
        }

        g.closePath();
        g.fill();
    }

    @Override
    public Polygon createPolygon(int inflation)
    {
        List<Coordinate> coordinateList = new ArrayList<>();
        InterpolatingShape firstSegment = (InterpolatingShape) getSegments().get(0);
        coordinateList.add(new Coordinate(firstSegment.getFrom().getX(), firstSegment.getFrom().getY()));
        segments.forEach(segment ->
        {
            if (segment instanceof LinearShape)
                coordinateList.add(new Coordinate(((LinearShape) segment).getTo().getX(), ((LinearShape) segment).getTo().getY()));
            else if (segment instanceof CircularShape)
                coordinateList.addAll(((CircularShape)segment).getLineStringCoordinates());
        });

        Coordinate[] coordinates = new Coordinate[coordinateList.size()];
        coordinateList.toArray(coordinates);
        return VectorToolPathGenerator.factory.createPolygon(coordinates);
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Region clone = (Region)super.clone();
        clone.segments = segments.stream().map(s -> {
            try
            {
                return (GerberPrimitive) s.clone();
            }
            catch (CloneNotSupportedException e) {}
            return null;
        }).collect(Collectors.toList());
        return clone;
    }
}
