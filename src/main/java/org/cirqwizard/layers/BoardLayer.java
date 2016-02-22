package org.cirqwizard.layers;

import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.List;

public class BoardLayer
{
    private List<LayerElement> elements;
    private List<Toolpath> toolpaths;

    public List<LayerElement> getElements()
    {
        return elements;
    }

    public void setElements(List<LayerElement> elements)
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
}
