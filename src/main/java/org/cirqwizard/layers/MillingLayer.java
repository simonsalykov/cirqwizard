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

package org.cirqwizard.layers;

import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.CircularShape;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.*;


public class MillingLayer extends Layer
{
    private ArrayList<GerberPrimitive> elements = new ArrayList<GerberPrimitive>();
    private ArrayList<Toolpath> toolpaths = new ArrayList<Toolpath>();

    public void setElements(ArrayList<GerberPrimitive> elements)
    {
        this.elements = elements;
    }

    public ArrayList<GerberPrimitive> getElements()
    {
        return elements;
    }

    public ArrayList<Toolpath> getToolpaths()
    {
        return toolpaths;
    }

    public void generateToolpaths()
    {
        toolpaths = new ArrayList<>();
        for (GerberPrimitive element : elements)
        {
            if (element instanceof LinearShape)
            {
                LinearShape shape = (LinearShape) element;
                toolpaths.add(new LinearToolpath(element.getAperture().getWidth(), shape.getFrom(), shape.getTo()));
            }
            else if (element instanceof CircularShape)
            {
                CircularShape shape = (CircularShape) element;
                toolpaths.add(new CircularToolpath(element.getAperture().getWidth(), shape.getFrom(), shape.getTo(), shape.getArc().getCenter(), shape.getArc().getRadius(),
                        shape.getArc().isClockwise()));
            }
        }
    }

    @Override
    public void rotate(boolean clockwise)
    {
        for (GerberPrimitive p : elements)
            p.rotate(clockwise);
    }

    @Override
    public void move(Point point)
    {
        for (GerberPrimitive p : elements)
            p.move(point);
    }

    @Override
    public Point getMinPoint()
    {
        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (GerberPrimitive p : elements)
        {
            if (p.getMin().getX() < min.getX())
                min = new Point(p.getMin().getX(), min.getY());
            if (p.getMin().getY() < min.getY())
                min = new Point(min.getX(), p.getMin().getY());
        }
        return min;
    }

    @Override
    public void clearSelection()
    {
    }
}