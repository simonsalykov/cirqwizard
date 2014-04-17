package org.cirqwizard.generation.optimizer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.*;

public class ChainDetector
{
    private static final int ROUNDING_FACTOR = 30;

    private List<Toolpath> toolpaths;

    private ArrayList<Point> vertices = new ArrayList<>();
    private HashMap<Point, ArrayList<Toolpath>> map = new HashMap<>();
    private DoubleProperty progressProperty = new SimpleDoubleProperty();
    private StringProperty estimatedMachiningTimeProperty = new SimpleStringProperty();

    public ChainDetector(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
        generateMap();
    }

    public List<Chain> detect()
    {
        List<Chain> result = new ArrayList<>();
        while (!vertices.isEmpty())
        {
            Point p = vertices.get(0);
            if (map.get(p).isEmpty())
            {
                vertices.remove(0);
                continue;
            }
            ArrayList<Toolpath> chainSegments = new ArrayList<>();
            traverse(chainSegments, p, (CuttingToolpath) map.get(p).get(0));
            result.add(new Chain(chainSegments));
        }

        return result;
    }

    public DoubleProperty progressProperty()
    {
        return progressProperty;
    }

    public StringProperty estimatedMachiningTimeProperty()
    {
        return estimatedMachiningTimeProperty;
    }

    private void traverse(ArrayList<Toolpath> result, Point point, CuttingToolpath edge)
    {
        if (edge.getCurve().getFrom().equals(point, ROUNDING_FACTOR))
            result.add(edge);
        else
        {
            if (edge instanceof LinearToolpath)
                result.add(new LinearToolpath(edge.getToolDiameter(), edge.getCurve().getTo(), edge.getCurve().getFrom()));
            else if (edge instanceof CircularToolpath)
            {
                Arc arc = (Arc) edge.getCurve();
                result.add(new CircularToolpath(edge.getToolDiameter(), arc.getTo(), arc.getFrom(),
                        arc.getCenter(), arc.getRadius(), !arc.isClockwise()));
            }
        }
        Point p = edge.getCurve().getFrom().round(ROUNDING_FACTOR);
        if (p.equals(point, ROUNDING_FACTOR))
            p = edge.getCurve().getTo().round(ROUNDING_FACTOR);
        map.get(point).remove(edge);

        if (map.get(p) != null)
        {
            map.get(p).remove(edge);
            if (!map.get(p).isEmpty())
                traverse(result, p, (CuttingToolpath) map.get(p).get(0));
        }
    }

    private void generateMap()
    {
        for (Toolpath t : toolpaths)
        {
            Point from = ((CuttingToolpath)t).getCurve().getFrom().round(ROUNDING_FACTOR);
            Point to = ((CuttingToolpath)t).getCurve().getTo().round(ROUNDING_FACTOR);

            ArrayList<Toolpath> list = map.get(from);
            if (list == null)
            {
                vertices.add(from);
                list = new ArrayList<>();
                map.put(from, list);
            }
            list.add(t);

            list = map.get(to);
            if (list == null)
            {
                vertices.add(to);
                list = new ArrayList<>();
                map.put(to, list);
            }
            list.add(t);
        }
        Collections.sort(vertices, new Comparator<Point>()
        {
            @Override
            public int compare(Point o1, Point o2)
            {
                if (o1.getX() < o2.getX())
                    return -1;
                if (o1.getX() > o2.getX())
                    return 1;
                if (o1.getY() < o2.getY())
                    return -1;
                return o1.getY() == o2.getY() ? 0 : 1;
            }
        });
    }
}
