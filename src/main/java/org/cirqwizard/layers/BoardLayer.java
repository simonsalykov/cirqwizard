package org.cirqwizard.layers;

import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.List;

public class BoardLayer
{
    private List<? extends LayerElement> elements;
    private List<Toolpath> toolpaths;

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
}
