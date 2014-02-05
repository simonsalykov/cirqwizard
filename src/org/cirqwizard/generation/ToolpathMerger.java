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
import java.util.HashMap;
import java.util.List;

public class ToolpathMerger
{
    private static final double MERGE_THRESHOLD = 20.0;
    private static final int ROUNDING_FACTOR = 10;
    private static final double ARC_CENTER_THRESHOLD = 50.0;

    private List<Toolpath> toolpaths;

    public ToolpathMerger(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
    }

    public List<Toolpath> merge()
    {
        double comparisonThreshold = Math.PI / 60;  // 3 degrees

        HashMap<Point, ArrayList<Toolpath>> map = getVerticesMap(toolpaths);

        ArrayList<Toolpath> toBeRemoved = new ArrayList<>();

        for (Point p : map.keySet())
        {
            ArrayList<Toolpath> toMerge = map.get(p);
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

                    if (t1 == t2)
                        continue;

                    boolean l1Inversed = false;
                    if (!c1.getTo().equals(p, MERGE_THRESHOLD))
                    {
                        c1 = c1.reverse();
                        l1Inversed = true;
                    }
                    if (!c2.getFrom().equals(p, MERGE_THRESHOLD))
                        c2 = c2.reverse();

                    if (t1 instanceof LinearToolpath && t2 instanceof LinearToolpath)
                    {
                        Line l1 = (Line) c1;
                        Line l2 = (Line) c2;
                        if (l2.getFrom().equals(l1.getTo(), MERGE_THRESHOLD))
                        {
                            if (l1.getFrom().equals(l2.getTo())) // Removing duplicate segments
                            {
                                toBeRemoved.add(t2);
                                map.get(c2.getTo().round(ROUNDING_FACTOR)).remove(t2);
                                continue;
                            }
                            else if (Math.abs(Math.abs(l1.angleToX() - l2.angleToX()) - Math.PI) < comparisonThreshold) // Removing overlapping segments
                            {
                                if (l1.length() > l2.length())
                                {
                                    toBeRemoved.add(t2);
                                    map.get(c2.getTo().round(ROUNDING_FACTOR)).remove(t2);
                                    continue;
                                }
                                else
                                {
                                    toBeRemoved.add(t1);
                                    map.get(c2.getTo().round(ROUNDING_FACTOR)).remove(t1);
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
                        if (a1.getTo().equals(a2.getFrom(), MERGE_THRESHOLD) && a1.isClockwise() == a2.isClockwise())
                        {
                            if (a1.getCenter().equals(a2.getCenter(), ARC_CENTER_THRESHOLD) &&
                                    Math.abs(a1.getRadius() - a2.getRadius()) < ARC_CENTER_THRESHOLD)
                                merge = true;
                        }
                    }

                    if (merge)
                    {
                        if (l1Inversed)
                            ((CuttingToolpath) t1).getCurve().setFrom(c2.getTo());
                        else
                            ((CuttingToolpath) t1).getCurve().setTo(c2.getTo());
                        toBeRemoved.add(t2);
                        map.get(c2.getTo().round(ROUNDING_FACTOR)).remove(t2);
                        map.get(c2.getTo().round(ROUNDING_FACTOR)).add(t1);
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

    private HashMap<Point, ArrayList<Toolpath>> getVerticesMap(List<Toolpath> toolpaths)
    {
        HashMap<Point, ArrayList<Toolpath>> map = new HashMap<>();
        for (Toolpath t : toolpaths)
        {
            Point from = ((CuttingToolpath)t).getCurve().getFrom().round(ROUNDING_FACTOR);
            Point to = ((CuttingToolpath)t).getCurve().getTo().round(ROUNDING_FACTOR);

            ArrayList<Toolpath> list = map.get(from);
            if (list == null)
            {
                list = new ArrayList<>();
                map.put(from, list);
            }
            list.add(t);

            list = map.get(to);
            if (list == null)
            {
                list = new ArrayList<>();
                map.put(to, list);
            }
            list.add(t);
        }

        return map;
    }

}
