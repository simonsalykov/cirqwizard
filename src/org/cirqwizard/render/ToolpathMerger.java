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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
        ToolpathsGraph graph = new ToolpathsGraph();
        for (Toolpath t : toolpaths)
            graph.addVertex(t);
        for (Point p : map.keySet())
        {
            ArrayList<Toolpath> toolpaths = map.get(p);
            for (int i = 0; i < toolpaths.size(); i++)
                for (int j = i + 1; j < toolpaths.size(); j++)
                    graph.addEdge(toolpaths.get(i), toolpaths.get(j));
        }

        ArrayList<ArrayList<Toolpath>> chains = new ArrayList<ArrayList<Toolpath>>();
        LinkedList<Toolpath> toolpathsToProcess = new LinkedList<Toolpath>(toolpaths);
        while (!toolpathsToProcess.isEmpty())
        {
            ArrayList<Toolpath> chain = new ArrayList<Toolpath>();
            Toolpath prev = null;
            for (Toolpath t : graph.traverse(toolpathsToProcess.getFirst()))
            {
                boolean merged = false;
                if (prev != null)
                {
                    if (t instanceof LinearToolpath && prev instanceof LinearToolpath)
                    {
                        Line prevLine = (Line) ((LinearToolpath) prev).getCurve();
                        Line line = (Line) ((LinearToolpath) t).getCurve();
                        if (!line.getFrom().equals(prevLine.getTo(), 0.02))
                            line = line.reverse();
                        if (line.getFrom().equals(prevLine.getTo(), 0.02))
                        {
                            if (prevLine.angleToX().subtract(line.angleToX()).abs().compareTo(MathUtil.PI.divide(60)) < 0)
                            {
                                System.out.println("merging " + prevLine + " and " + line);
                                prevLine.setTo(line.getTo());
                                merged = true;
                            }
                        }
                    }
                    else if (t instanceof CircularToolpath && prev instanceof CircularToolpath)
                    {
                        Arc prevArc = (Arc) ((CircularToolpath) prev).getCurve();
                        Arc arc = (Arc) ((CircularToolpath) t).getCurve();
                        if (prevArc.getTo().equals(arc.getFrom(), 0.02))
                        {
                            if (prevArc.getCenter().equals(arc.getCenter(), 0.05) && Math.abs(prevArc.getRadius().doubleValue() - arc.getRadius().doubleValue()) < 0.05)
                            {
                                System.out.println("merging " + prevArc + " and " + arc);
                                prevArc.setTo(arc.getTo());
                                merged = true;
                            }
                        }
                    }
                }
                if (!merged)
                {
                    chain.add(t);
                    prev = t;
                }
                toolpathsToProcess.remove(t);
            }
            chains.add(chain);
        }
        System.out.println("chains: " + chains.size());

        ArrayList<Toolpath> result = new ArrayList<Toolpath>();
        for (ArrayList<Toolpath> at : chains)
            for (Toolpath t : at)
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

    private static class ToolpathsGraph
    {
        private ArrayList<Toolpath> vertices = new ArrayList<Toolpath>();
        private ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();

        public void addVertex(Toolpath t)
        {
            vertices.add(t);
            edges.add(new ArrayList<Integer>());
        }

        public void addEdge(Toolpath vertex1, Toolpath vertex2)
        {
            int index1 = vertices.indexOf(vertex1);
            int index2 = vertices.indexOf(vertex2);
            if (!edges.get(index1).contains(vertex2))
                edges.get(index1).add(index2);
            if (!edges.get(index2).contains(vertex1))
                edges.get(index2).add(index1);
        }

        public List<Toolpath> traverse(Toolpath toolpath)
        {
            ArrayList<Toolpath> result = new ArrayList<Toolpath>();
            _traverse(vertices.indexOf(toolpath), new boolean[vertices.size()], result);
            return result;
        }

        private void _traverse(int index, boolean[] visited, ArrayList<Toolpath> result)
        {
            visited[index] = true;
            result.add(vertices.get(index));
            for (int i : edges.get(index))
                if (!visited[i])
                    _traverse(i, visited, result);
        }
    }

}
