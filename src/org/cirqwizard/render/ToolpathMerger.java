package org.cirqwizard.render;

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: simon
 * Date: 08/11/13
 * Time: 23:01
 */
public class ToolpathMerger
{
    private List<Toolpath> toolpaths;

    public ToolpathMerger(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
    }

    public List<Toolpath> merge()
    {
        HashMap<Point, ArrayList<Toolpath>> map = getVerticesMap(toolpaths);

        ArrayList<Toolpath> toBeRemoved = new ArrayList<Toolpath>();

        for (Point p : map.keySet())
        {
            ArrayList<Toolpath> toMerge = map.get(p);
            for (int i = 0; i < toMerge.size(); i++)
            {
                for (int j = i + 1; j < toMerge.size(); j++)
                {
                    Toolpath t1 = toMerge.get(i);
                    Toolpath t2 = toMerge.get(j);

                    if (!t1.getClass().equals(t2.getClass()))
                        continue;

                    Curve c1 = ((CuttingToolpath)t1).getCurve();
                    Curve c2 = ((CuttingToolpath)t2).getCurve();

                    boolean l1Inversed = false;
                    if (!c1.getTo().equals(p, 0.02))
                    {
                        c1 = c1.reverse();
                        l1Inversed = true;
                    }
                    if (!c2.getFrom().equals(p, 0.02))
                        c2 = c2.reverse();

                    boolean merge = false;
                    if (t1 instanceof LinearToolpath && t2 instanceof LinearToolpath)
                    {
                        Line l1 = (Line) c1;
                        Line l2 = (Line) c2;
                        if (l2.getFrom().equals(l1.getTo(), 0.02))
                        {
                            double comparisonThreshold = Math.PI / 60;  // 3 degrees
                            double angleDifference = l1.angleToX().subtract(l2.angleToX()).abs().doubleValue();
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
                        if (a1.getTo().equals(a2.getFrom(), 0.02))
                        {
                            if (a1.getCenter().equals(a2.getCenter(), 0.05) && Math.abs(a1.getRadius().doubleValue() - a2.getRadius().doubleValue()) < 0.05)
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
                        map.get(c2.getTo().round()).remove(t2);
                        map.get(c2.getTo().round()).add(t1);
                        break;

                    }

                }
            }
        }

        ArrayList<Toolpath> result = new ArrayList<Toolpath>();
        for (Toolpath t : toolpaths)
            if (!toBeRemoved.contains(t))
                result.add(t);

        return result;
    }

    private HashMap<Point, ArrayList<Toolpath>> getVerticesMap(List<Toolpath> toolpaths)
    {
        HashMap<Point, ArrayList<Toolpath>> map = new HashMap<Point, ArrayList<Toolpath>>();
        for (Toolpath t : toolpaths)
        {
            Point from = ((CuttingToolpath)t).getCurve().getFrom().round();
            Point to = ((CuttingToolpath)t).getCurve().getTo().round();

            ArrayList<Toolpath> list = map.get(from);
            if (list == null)
            {
                list = new ArrayList<Toolpath>();
                map.put(from, list);
            }
            list.add(t);

            list = map.get(to);
            if (list == null)
            {
                list = new ArrayList<Toolpath>();
                map.put(to, list);
            }
            list.add(t);
        }

        return map;
    }

}
