package org.cirqwizard.render;

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.math.MathUtil;
import org.cirqwizard.math.RealNumber;
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

        ArrayList<Point> vertices = new ArrayList<Point>(map.keySet());
        Collections.sort(vertices, new Comparator<Point>()
        {
            @Override
            public int compare(Point o1, Point o2)
            {
                return o1.getX().compareTo(o2.getX()) != 0 ? o1.getX().compareTo(o2.getX()) : o1.getY().compareTo(o2.getY());
            }
        });
        for (Point p : vertices)
            System.out.println("%% " + p + " [" + map.get(p).size() + "]");

        ArrayList<Toolpath> toBeRemoved = new ArrayList<Toolpath>();
        HashMap<Toolpath, Toolpath> replacements = new HashMap<Toolpath, Toolpath>();

        for (Point p : map.keySet())
        {
            ArrayList<Toolpath> toMerge = map.get(p);
            for (int i = 0; i < toMerge.size(); i++)
            {
                for (int j = i + 1; j < toMerge.size(); j++)
                {
                    Toolpath t1 = toMerge.get(i);
                    Toolpath t2 = toMerge.get(j);

//                    if (replacements.get(t1) != null)
//                        t1 = replacements.get(t1);
//                    if (replacements.get(t2) != null)
//                        t2 = replacements.get(t2);
//                    if (t1 == t2)
//                        continue;

                    if (t1 instanceof LinearToolpath && t2 instanceof LinearToolpath)
                    {
                        Line l1 = (Line) ((LinearToolpath) t1).getCurve();
                        Line l2 = (Line) ((LinearToolpath) t2).getCurve();
                        boolean l1Inversed = false;
                        if (!l1.getTo().equals(p, 0.02))
                        {
                            l1 = l1.reverse();
                            l1Inversed = true;
                        }
                        if (!l2.getFrom().equals(p, 0.02))
                            l2 = l2.reverse();
                        if (l2.getFrom().equals(l1.getTo(), 0.02))
                        {
                            if (l1.angleToX().subtract(l2.angleToX()).abs().compareTo(MathUtil.PI.divide(60)) < 0)// ||
//                                    l1.angleToX().subtract(l2.angleToX()).abs().subtract(MathUtil.PI).abs().compareTo(MathUtil.PI.divide(60)) < 0)
                            {
                                System.out.println("merging " + l1 + " and " + l2);
                                if (l1Inversed)
                                    ((LinearToolpath) t1).getCurve().setFrom(l2.getTo());
                                else
                                    ((LinearToolpath) t1).getCurve().setTo(l2.getTo());
//                                l1.setTo(l2.getTo());
                                toBeRemoved.add(t2);
                                map.get(l2.getTo().round()).remove(t2);
                                map.get(l2.getTo().round()).add(t1);
                                replacements.put(t2, t1);
                                break;
                            }
                            else
                                System.out.println("NOT merging " + l1 + " and " + l2);
                        }
                        else
                            System.out.println("WTH: " + l1 + " and " + l2);
                    }
                    else if (t1 instanceof CircularToolpath && t2 instanceof CircularToolpath)
                    {
                        Arc a1 = (Arc) ((CircularToolpath) t1).getCurve();
                        Arc a2 = (Arc) ((CircularToolpath) t2).getCurve();
                        if (a1.getTo().equals(a2.getFrom(), 0.02))
                        {
                            if (a1.getCenter().equals(a2.getCenter(), 0.05) && Math.abs(a1.getRadius().doubleValue() - a2.getRadius().doubleValue()) < 0.05)
                            {
                                System.out.println("merging " + a1 + " and " + a2);
                                a1.setTo(a2.getTo());
                                toBeRemoved.add(t2);
                                replacements.put(t2, t1);
                                break;
                            }
                        }
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
