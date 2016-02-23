package org.cirqwizard.layers;

import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.geom.Point;

import java.util.List;

public class BoardLayer
{
    private List<? extends LayerElement> elements;
    private List<Toolpath> toolpaths;

    public BoardLayer()
    {
    }

    public BoardLayer(List<? extends LayerElement> elements)
    {
        this.elements = elements;
    }

    public List<? extends LayerElement> getElements()
    {
        return elements;
    }

    public void setElements(List<? extends LayerElement> elements)
    {
        this.elements = elements;
    }

    public List<Toolpath> getToolpaths()
    {
        return toolpaths;
    }

    public void setToolpaths(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
    }

    public Point getMinPoint()
    {
        int minX = elements.stream().mapToInt(p -> p.getMin().getX()).min().getAsInt();
        int minY = elements.stream().mapToInt(p -> p.getMin().getY()).min().getAsInt();
        return new Point(minX, minY);
    }

    public Point getMaxPoint()
    {
        int maxX = elements.stream().mapToInt(p -> p.getMax().getX()).max().getAsInt();
        int maxY = elements.stream().mapToInt(p -> p.getMax().getY()).max().getAsInt();
        return new Point(maxX, maxY);
    }

    public void move(Point p)
    {
        elements.stream().forEach(e -> e.move(p));
    }

}
