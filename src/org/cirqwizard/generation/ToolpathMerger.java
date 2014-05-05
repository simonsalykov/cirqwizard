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

package org.cirqwizard.generation;

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ToolpathMerger
{
    private List<Toolpath> toolpaths;
    private HashMap<Point, ArrayList<Toolpath>> verticesMap = new HashMap<>();
    private int tolerance;
    private int roundingFactor;

    public ToolpathMerger(List<Toolpath> toolpaths, int tolerance)
    {
        this.toolpaths = toolpaths;
        this.tolerance = tolerance;
        this.roundingFactor = tolerance * 2;
    }

    public List<Toolpath> merge()
    {
        double comparisonThreshold = Math.PI / 60;  // 3 degrees

        initVerticesMap(toolpaths);

        ArrayList<Toolpath> toBeRemoved = new ArrayList<>();

        for (Point p : verticesMap.keySet())
        {

            ArrayList<Toolpath> toMerge = verticesMap.get(p);
            for (int i = 0; i < toMerge.size(); i++)
            {
                boolean merge = false;

                for (int j = i + 1; j < toMerge.size(); j++)
                {
                    Toolpath t1 = toMerge.get(i);
                    Toolpath t2 = toMerge.get(j);

                    if (!t1.getClass().equals(t2.getClass()))
                        continue;

                    Curve c1 = ((CuttingToolpath)t1).getCurve();
                    Curve c2 = ((CuttingToolpath)t2).getCurve();

                    if (t1 == t2 || toBeRemoved.contains(t1) || toBeRemoved.contains(t2))
                        continue;

                    boolean l1Inversed = false;
                    if (!c1.getTo().equals(p, roundingFactor))
                    {
                        c1 = c1.reverse();
                        l1Inversed = true;
                    }
                    if (!c2.getFrom().equals(p, roundingFactor))
                        c2 = c2.reverse();

                    if (t1 instanceof LinearToolpath && t2 instanceof LinearToolpath)
                    {
                        Line l1 = (Line) c1;
                        Line l2 = (Line) c2;
                        if (l2.getFrom().equals(l1.getTo(), tolerance))
                        {
                            if (l1.getFrom().equals(l2.getTo())) // Removing duplicate segments
                            {
                                toBeRemoved.add(t2);
                                removeVertices(c2.getTo(), t2);
                                continue;
                            }
                            else if (Math.abs(Math.abs(l1.angleToX() - l2.angleToX()) - Math.PI) < comparisonThreshold) // Removing overlapping segments
                            {
                                if (l1.length() > l2.length())
                                {
                                    toBeRemoved.add(t2);
                                    removeVertices(c2.getTo(), t2);
                                    continue;
                                }
                                else
                                {
                                    toBeRemoved.add(t1);
                                    removeVertices(c1.getFrom(), t1);
                                    break;
                                }
                            }

                            double angleDifference = Math.abs(l1.angleToX() - l2.angleToX());
                            while (angleDifference >= Math.PI - comparisonThreshold)
                                angleDifference -= Math.PI;
                            angleDifference = Math.abs(angleDifference);

                            if (angleDifference < comparisonThreshold)
                                merge = true;
                        }
                    }
                    else if (t1 instanceof CircularToolpath && t2 instanceof CircularToolpath)
                    {
                        Arc a1 = (Arc) c1;
                        Arc a2 = (Arc) c2;
                        if (a1.getTo().equals(a2.getFrom(), tolerance) && a1.isClockwise() == a2.isClockwise())
                        {
                            if (a1.getCenter().equals(a2.getCenter()) && a1.getRadius() == a2.getRadius())
                                merge = true;
                        }
                    }

                    if (merge)
                    {
                        Curve curve = ((CuttingToolpath) t1).getCurve();
                        if (l1Inversed)
                            curve.setFrom(c2.getTo());
                        else
                            curve.setTo(c2.getTo());
                        if (curve instanceof Arc && c2.getTo().equals(c1.getFrom(), tolerance))
                            curve.setTo(curve.getFrom());
                        toBeRemoved.add(t2);
                        removeVertices(c2.getFrom(), t2);
                        removeVertices(c2.getTo(), t2);
                        addVertices(c2.getTo(), t1);
                        break;

                    }
                }

                if (merge)
                    break;
            }
        }

        int lines = 0;
        int arcs = 0;
        ArrayList<Toolpath> result = new ArrayList<>();
        for (Toolpath t : toolpaths)
            if (!toBeRemoved.contains(t))
            {
                if (t instanceof LinearToolpath)
                    lines++;
                if (t instanceof CircularToolpath)
                    arcs++;
                result.add(t);
            }
        System.out.println("lines: " + lines + ", arcs: " + arcs);

        return result;
    }

    private void initVerticesMap(List<Toolpath> toolpaths)
    {
        for (Toolpath t : toolpaths)
        {
            Curve curve = ((CuttingToolpath) t).getCurve();
            addVertices(curve.getFrom(), t);
            addVertices(curve.getTo(), t);
        }
    }

    private List<Point> getPointsForVertex(Point point)
    {
        Point p1 = new Point(point.getX() / roundingFactor * roundingFactor, point.getY() / roundingFactor * roundingFactor);
        Point p2 = p1.add(new Point(roundingFactor, 0));
        Point p3 = p1.add(new Point(0, roundingFactor));
        Point p4 = p1.add(new Point(roundingFactor, roundingFactor));
        return Arrays.asList(p1, p2, p3, p4);
    }

    private void addVertices(Point point, Toolpath toolpath)
    {
        addVertex(getPointsForVertex(point), toolpath);
    }

    private void removeVertices(Point point, Toolpath toolpath)
    {
        removeVertex(getPointsForVertex(point), toolpath);
    }

    private void addVertex(List<Point> vertices, Toolpath toolpath)
    {
        for (Point p : vertices)
        {
            ArrayList<Toolpath> list = verticesMap.get(p);
            if (list == null)
            {
                list = new ArrayList<>();
                verticesMap.put(p, list);
            }
            if (!list.contains(toolpath))
                list.add(toolpath);
        }
    }

    private void removeVertex(List<Point> vertices, Toolpath toolpath)
    {
        for (Point p : vertices)
        {
            ArrayList<Toolpath> list = verticesMap.get(p);
            if (list != null)
                list.remove(toolpath);
        }
    }

}
